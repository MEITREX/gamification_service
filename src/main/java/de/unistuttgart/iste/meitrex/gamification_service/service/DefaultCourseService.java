package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.client.CourseServiceGraphQLClient;
import de.unistuttgart.iste.meitrex.gamification_service.time.IPeriodCalculator;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import de.unistuttgart.iste.meitrex.gamification_service.time.Period;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;


/**
 * {@link DefaultCourseService} implements logic handling local course creation on demand. Consider extracting an
 * interface, if respective course-related logic shall be exposed via this service's interface, e.g. its GraphQL
 * layer.
 *
 * @author Philipp Kunz
 * */
@Slf4j
@Component
class DefaultCourseService {


    private static final String LEADERBOARD_TITLE_TEMPLATE = "Leaderboard - %s - %s";

    private final ILeaderboardRepository leaderboardRepository;

    private final ICourseRepository courseRepository;

    private final CourseServiceGraphQLClient graphQLClient;

    private final ITimeService timeService;

    private final IPeriodCalculator periodCalculator;

    public DefaultCourseService(
            @Autowired ICourseRepository courseRepository,
            @Autowired ILeaderboardRepository leaderboardRepository,
            @Autowired CourseServiceGraphQLClient graphQLClient,
            @Autowired ITimeService timeService,
            @Autowired IPeriodCalculator periodCalculator
    ) {
        this.leaderboardRepository = Objects.requireNonNull(leaderboardRepository);
        this.courseRepository = Objects.requireNonNull(courseRepository);
        this.graphQLClient = Objects.requireNonNull(graphQLClient);
        this.timeService = Objects.requireNonNull(timeService);
        this.periodCalculator = Objects.requireNonNull(periodCalculator);
    }

    /**
     * Fetches an instance of {@link CourseEntity} identified by its {@link UUID} from the underlying persistence mechanism,
     * if no such instance exists, it is created, initialized, persisted and finally returned.
     *
     * @param courseId the {@link UUID} identifying the {@link CourseEntity}
     * @return an instance of {@link CourseEntity} featuring the passed courseId.
     */
    CourseEntity fetchOrCreate(UUID courseId) {
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
                this.graphQLClient.findCourseName(uuid).ifPresent(courseEntity::setTitle);
                courseEntity.setId(uuid);
                persistentCourseEntity = this.courseRepository.save(courseEntity);
                this.initCourseOnCreation(persistentCourseEntity);
                break;
            } catch (IOException e0) {
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
    private void initCourseOnCreation(CourseEntity courseEntity) {
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
