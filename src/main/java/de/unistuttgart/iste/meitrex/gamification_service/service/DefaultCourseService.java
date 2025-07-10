package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.LeaderboardEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ICourseRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ILeaderboardRepository;
import de.unistuttgart.iste.meitrex.gamification_service.time.IPeriodCalculator;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import de.unistuttgart.iste.meitrex.gamification_service.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

/**
 * Bundles logic for handling instances of {@link CourseEntity}. Consider extracting an interface, once respective
 * business logic shall be exposed for external usage.
 *
 * @author Philipp Kunz
 *
 */
@Component
class DefaultCourseService {

    private final ITimeService timeService;

    private final IPeriodCalculator periodCalculator;

    private final ICourseRepository courseRepository;

    private final ILeaderboardRepository leaderboardRepository;

    public DefaultCourseService(
            @Autowired ITimeService timeService,
            @Autowired IPeriodCalculator periodCalculator,
            @Autowired ICourseRepository courseRepository,
            @Autowired ILeaderboardRepository leaderboardRepository
    ) {
        this.timeService = Objects.requireNonNull(timeService);
        this.periodCalculator = Objects.requireNonNull(periodCalculator);
        this.courseRepository = Objects.requireNonNull(courseRepository);
        this.leaderboardRepository = Objects.requireNonNull(leaderboardRepository);
    }

    /**
     * Fetches an instance of {@link CourseEntity} featuring the passed courseId. If no such entity exists, it is created.
     *
     * @param courseId instance of {@link UUID} identifying an instance of {@link CourseEntity}.
     * @return an instance of {@link CourseEntity} featuring the passed courseId.
     */
    CourseEntity fetchOrCreate(UUID courseId) {

        //TODO Fetching course name via GraphQL.

        return this.courseRepository
                .findById(courseId)
                .orElseGet(()  -> setupCourse(courseId));
    }

    private CourseEntity setupCourse(UUID uuid) {
        final CourseEntity courseEntity = new CourseEntity();
        courseEntity.setId(uuid);
        courseEntity.setTitle("");
        final CourseEntity persistentCourseEntity = this.courseRepository
                .save(courseEntity);
        Arrays.stream(Period.values())
                .forEach(period -> this.createAndAddLeaderboard(persistentCourseEntity, period));
        return persistentCourseEntity;
    }

    private void createAndAddLeaderboard(CourseEntity courseEntity, Period period) {
        LeaderboardEntity leaderboard = new LeaderboardEntity();
        leaderboard.setCourse(courseEntity);
        leaderboard.setPeriod(period);
        // Intentionally using the first of the current month as the start date for ALL_TIME leaderboards.
        leaderboard.setStartDate(this.periodCalculator.calcStartDate(this.timeService.now(), Period.ALL_TIME.equals(period) ? Period.MONTHLY : period));
        leaderboard.setTitle("");
        leaderboard = this.leaderboardRepository.save(leaderboard);
        courseEntity.getLeaderboardEntityList().add(leaderboard);
    }
}
