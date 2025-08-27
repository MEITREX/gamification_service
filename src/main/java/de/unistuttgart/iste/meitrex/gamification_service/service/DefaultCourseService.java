package de.unistuttgart.iste.meitrex.gamification_service.service;

import java.util.*;

import de.unistuttgart.iste.meitrex.course_service.client.CourseServiceClient;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements.ICourseAchievementMapper;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseCreator;
import de.unistuttgart.iste.meitrex.gamification_service.time.IPeriodCalculator;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import de.unistuttgart.iste.meitrex.gamification_service.time.Period;
import de.unistuttgart.iste.meitrex.generated.dto.Chapter;
import de.unistuttgart.iste.meitrex.generated.dto.Course;
import lombok.extern.slf4j.*;
import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.*;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.*;


/**
 * {@link DefaultCourseService} implements logic handling local course creation on demand. Consider extracting an
 * interface, if respective course-related logic shall be exposed via this service's interface, e.g. its GraphQL
 * layer.
 *
 * @author Philipp Kunz
 * */
@Slf4j
@Component
class DefaultCourseService implements ICourseCreator {


    private static final String LEADERBOARD_TITLE_TEMPLATE = "Leaderboard - %s - %s";

    private final ILeaderboardRepository leaderboardRepository;

    private final ICourseRepository courseRepository;

    private final AchievementRepository achievementRepository;

    private final CourseServiceClient graphQLCourseClient;

    private final ITimeService timeService;

    private final IPeriodCalculator periodCalculator;

    private final ICourseAchievementMapper achievementMapper;

    public DefaultCourseService(
            @Autowired ICourseRepository courseRepository,
            @Autowired AchievementRepository achievementRepository,
            @Autowired ILeaderboardRepository leaderboardRepository,
            @Autowired CourseServiceClient graphQLClient,
            @Autowired ITimeService timeService,
            @Autowired IPeriodCalculator periodCalculator,
            @Autowired ICourseAchievementMapper achievementMapper
    ) {
        this.leaderboardRepository = Objects.requireNonNull(leaderboardRepository);
        this.achievementRepository = Objects.requireNonNull(achievementRepository);
        this.courseRepository = Objects.requireNonNull(courseRepository);
        this.graphQLCourseClient = Objects.requireNonNull(graphQLClient);
        this.timeService = Objects.requireNonNull(timeService);
        this.periodCalculator = Objects.requireNonNull(periodCalculator);
        this.achievementMapper = Objects.requireNonNull(achievementMapper);
    }

    @Override
    public CourseEntity fetchOrCreate(UUID courseId) {
        return this.courseRepository
                .findById(courseId)
                .orElseGet(()  -> setupCourse(courseId));
    }

    private CourseEntity setupCourse(UUID uuid) {
        CourseEntity persistentCourseEntity = null;
        final int MAX_ATTEMPT_COUNT = 3;
        int curAttempt = 1;
        while(curAttempt++ <= MAX_ATTEMPT_COUNT) {
            try {
                final CourseEntity courseEntity = new CourseEntity();
                final Course remoteCourse = this.graphQLCourseClient.queryCourseById(uuid);
                final List<Chapter> chapters = graphQLCourseClient.queryChapterByCourseId(uuid);
                if(Objects.nonNull(remoteCourse)) {
                    courseEntity.setTitle(remoteCourse.getTitle());
                }
                courseEntity.setId(uuid);
                courseEntity.setChapters(chapters);
                persistentCourseEntity = this.courseRepository.save(courseEntity);
                this.initCourseOnCreation(persistentCourseEntity);
                break;
            } catch (Exception e0) {
                if(curAttempt == MAX_ATTEMPT_COUNT) {
                    log.error("Final attempt of fetching the course name for {} failed.", uuid, e0);
                    throw new RuntimeException(e0);
                }
                else {
                    log.warn("Failed to fetch course name for course ID {}. Retrying.", uuid, e0);
                }
            }
        }
        return persistentCourseEntity;
    }

    // Creating Dependencies

    private void initCourseOnCreation(CourseEntity courseEntity) {
        initAchievements(courseEntity);
        initCourseLeaderboard(courseEntity);
    }

    private void initAchievements(CourseEntity courseEntity) {
        courseEntity.setAchievements(achievementRepository.saveAll(achievementMapper.map(courseEntity)));
    }

    private void initCourseLeaderboard(CourseEntity courseEntity) {
        Arrays.stream(Period.values()).forEach(period -> {
            LeaderboardEntity leaderboard = new LeaderboardEntity();
            leaderboard.setCourse(courseEntity);
            leaderboard.setPeriod(period);
            leaderboard.setStartDate(this.periodCalculator.calcStartDate(this.timeService.curTime(), Period.ALL_TIME.equals(period) ? Period.MONTHLY : period));
            leaderboard.setTitle(String.format(LEADERBOARD_TITLE_TEMPLATE, courseEntity.getTitle(), period));
            leaderboard = this.leaderboardRepository.save(leaderboard);
            courseEntity.getLeaderboardEntityList().add(leaderboard);
        });
    }


}
