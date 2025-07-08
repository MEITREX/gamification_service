package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.events.internal.InternalUserProgressUpdatedEvent;

import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentUserProgressUpdatedRepository;
import de.unistuttgart.iste.meitrex.gamification_service.exception.ResourceNotFoundException;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.LeaderboardEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ICourseRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ILeaderboardRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserScoreRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.scoring.IScoringFunction;
import de.unistuttgart.iste.meitrex.gamification_service.time.IPeriodCalculator;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import de.unistuttgart.iste.meitrex.gamification_service.time.Period;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cglib.core.Local;
import org.springframework.context.event.EventListener;
import org.springframework.data.repository.query.Param;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.*;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;


@Component
class DefaultLeaderboardService implements ILeaderboardService {

    private static final String ERR_MSG_NO_SUCH_PERSISTENT_USER_PROGRESS_UPDATED_EVENT = "";


    private static boolean isDayInRange(LocalDate day, LocalDate beginDate, LocalDate endDate) {
        return !day.isBefore(beginDate)  && day.isBefore(endDate);
    }


    private final IScoringFunction scoringFunction;

    private final DefaultUserService userService;

    private final DefaultCourseService courseService;

    private final IUserScoreRepository userScoreRepository;

    private final ILeaderboardRepository leaderboardRepository;

    private final ICourseRepository courseRepository;

    private final IPersistentUserProgressUpdatedRepository userProgressUpdatedRepository;

    private final IPeriodCalculator periodCalculator;

    private final ITimeService timeService;


    public DefaultLeaderboardService(
            @Autowired IScoringFunction scoringFunction,
            @Autowired IUserScoreRepository userScoreRepository,
            @Autowired ILeaderboardRepository leaderboardRepository,
            @Autowired ICourseRepository courseRepository,
            @Autowired DefaultUserService userService,
            @Autowired DefaultCourseService courseService,
            @Autowired IPersistentUserProgressUpdatedRepository userProgressUpdatedRepository,
            @Autowired IPeriodCalculator periodCalculator,
            @Autowired ITimeService timeService
    ) {
        this.scoringFunction = Objects.requireNonNull(scoringFunction);
        this.userScoreRepository = Objects.requireNonNull(userScoreRepository);
        this.leaderboardRepository = Objects.requireNonNull(leaderboardRepository);
        this.courseRepository = Objects.requireNonNull(courseRepository);
        this.userService = Objects.requireNonNull(userService);
        this.courseService = Objects.requireNonNull(courseService);
        this.userProgressUpdatedRepository = userProgressUpdatedRepository;
        this.periodCalculator = Objects.requireNonNull(periodCalculator);
        this.timeService = Objects.requireNonNull(timeService);
    }


    // Scheduled Logic

    @Transactional
    @Scheduled(cron = "0 0 0 * * MON")
    public void runWeeklyLeaderboardUpdate() {
        this.courseRepository
                .findAll()
                .forEach(courseEntity -> this.updateCourseLeaderboard(courseEntity, LocalDate.now()));
    }

    @Transactional
    @Scheduled(cron = "0 0 0 1 * *")
    public void runMonthlyLeaderboardUpdate() {
        this.courseRepository
                .findAll()
                .forEach(courseEntity -> this.updateCourseLeaderboard(courseEntity, LocalDate.now()));
    }

    @Transactional
    @Scheduled(fixedRate = 5000)
    public void retryFailedPersistentUserProgressUpdatedEventsProcessing() {
        this.userProgressUpdatedRepository
                .findByStatus(PersistentEvent.Status.FAILED_RETRY)
                .forEach(this::handlePersistentUserProgressUpdatedEvent);
    }

    // Event listener

    @Transactional
    @EventListener
    public void onUserProgressUpdated(InternalUserProgressUpdatedEvent event) {
        final UUID id = Objects.requireNonNull(event.getId());
        final PersistentUserProgressUpdatedEvent persistentEvent
                = this.userProgressUpdatedRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ERR_MSG_NO_SUCH_PERSISTENT_USER_PROGRESS_UPDATED_EVENT));
        handlePersistentUserProgressUpdatedEvent(persistentEvent);
    }


    // Leaderboard update logic

    private void updateCourseLeaderboard(CourseEntity courseEntity, LocalDate now){

        this.leaderboardRepository
                .findByCourseAndPeriodOrderByStartDateDesc(courseEntity, Period.WEEKLY)
                .stream()
                .findFirst()
                .ifPresent(leaderboard -> this.updateCourseLeaderboard(courseEntity, leaderboard, now, Period.WEEKLY));

        this.leaderboardRepository
                .findByCourseAndPeriodOrderByStartDateDesc(courseEntity, Period.MONTHLY)
                .stream()
                .findFirst()
                .ifPresent(leaderboard -> this.updateCourseLeaderboard(courseEntity, leaderboard, now, Period.MONTHLY));

    }


    private void updateCourseLeaderboard(CourseEntity courseEntity, LeaderboardEntity leaderboardEntity,  LocalDate now, Period period) {

        final LocalDate curStartDate = leaderboardEntity.getStartDate();
        final LocalDate nextStartDate = this.periodCalculator.calcSucStartDate(leaderboardEntity.getStartDate(), period);

        // Check if the current date now is still covered by the most recent passed leader board.
        if(isDayInRange(now, curStartDate, nextStartDate)) {
            return;
        }

        // Instantiation and initialization. TODO Creating a meaningful leaderboard title.
        LeaderboardEntity newLeaderboard = new LeaderboardEntity();
        newLeaderboard.setTitle("");
        newLeaderboard.setStartDate(nextStartDate);
        newLeaderboard.setPeriod(period);
        newLeaderboard.setCourse(courseEntity);

        // Updating references.
        courseEntity.getLeaderboardEntityList().add(newLeaderboard);
        newLeaderboard.setCourse(courseEntity);

        //Persist
        newLeaderboard = this.leaderboardRepository.save(newLeaderboard);

        //Recursion
        updateCourseLeaderboard(courseEntity, newLeaderboard, now, period);
    }



    private void handlePersistentUserProgressUpdatedEvent(PersistentUserProgressUpdatedEvent event) {

        final long now = this.timeService.now();
        final LocalDate today = Instant.ofEpochMilli(now)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        final UserEntity userEntity = this.userService.fetchOrCreate(event.getUserId());
        final CourseEntity courseEntity = this.courseService.fetchOrCreate(event.getCourseId());

        final Optional<LeaderboardEntity> allTimeLeaderboard = this.leaderboardRepository
                .findByCourseAndPeriodOrderByStartDateDesc(courseEntity, Period.ALL_TIME);
        final Optional<LeaderboardEntity> weeklyLeaderboard = this.leaderboardRepository
                .findByCourseAndPeriodOrderByStartDateDesc(courseEntity, Period.WEEKLY);
        final Optional<LeaderboardEntity> monthlyLeaderboard = this.leaderboardRepository
                .findByCourseAndPeriodOrderByStartDateDesc(courseEntity, Period.MONTHLY);

        if(allTimeLeaderboard.isEmpty() || weeklyLeaderboard.isEmpty() || monthlyLeaderboard.isEmpty()) {
            this.handleUnmetDependency(event, now);
            return;
        }

        final LocalDate weeklyCurStartDate = weeklyLeaderboard.get().getStartDate();
        final LocalDate monthlyCurStartDate = monthlyLeaderboard.get().getStartDate();
        final LocalDate weeklyNextStartDate = this.periodCalculator.calcSucStartDate(weeklyCurStartDate, Period.WEEKLY);
        final LocalDate monthlyNextStartDate = this.periodCalculator.calcSucStartDate(monthlyCurStartDate, Period.MONTHLY);

        if(!isDayInRange(today, weeklyCurStartDate, weeklyNextStartDate) || !isDayInRange(today, monthlyCurStartDate,  monthlyNextStartDate)) {
            this.handleUnmetDependency(event, now);
            return;
        }

        final UserScoreEntity allTimeUserScoreEntity = this.findOrCreateMostRecentUserScoreEntity(allTimeLeaderboard.get(), userEntity, courseEntity, Period.ALL_TIME);
        final UserScoreEntity weeklyUserScoreEntity = this.findOrCreateMostRecentUserScoreEntity(weeklyLeaderboard.get(), userEntity, courseEntity, Period.WEEKLY);
        final UserScoreEntity motnhlyUserScoreEntity = this.findOrCreateMostRecentUserScoreEntity(monthlyLeaderboard.get(), userEntity, courseEntity, Period.MONTHLY);

        this.updateUserScoreEntity(allTimeUserScoreEntity, event);
        this.updateUserScoreEntity(weeklyUserScoreEntity, event);
        this.updateUserScoreEntity(motnhlyUserScoreEntity, event);
    }

    private void updateUserScoreEntity(UserScoreEntity userScoreEntity, PersistentUserProgressUpdatedEvent event) {
        final double curScore = userScoreEntity.getScore();
        final double newAdditionalScore = this.scoringFunction.score(event.getCorrectness(), event.getUserAttempt());
        userScoreEntity.setScore(curScore + newAdditionalScore);
    }

    private void handleUnmetDependency(PersistentUserProgressUpdatedEvent event, Long lastProcessingTimestamp) {
        int newCount = event.getAttemptCount() + 1;
        event.setAttemptCount(newCount);
        event.setLastProcessingAttemptTimestamp(lastProcessingTimestamp);
        PersistentEvent.Status newStatus = newCount >= event.getMaxCount() ? PersistentEvent.Status.FAILED : PersistentEvent.Status.FAILED_RETRY;
        event.setStatus(newStatus);
    }

    private UserScoreEntity findOrCreateMostRecentUserScoreEntity(LeaderboardEntity leaderboardEntity, UserEntity user, CourseEntity course, Period period
    ) {
        return this.userScoreRepository
                .findMostRecentUserScore(user, course, period)
                .orElseGet(() -> {
                    final UserScoreEntity scoreEntity = new UserScoreEntity();
                    scoreEntity.setLeaderboard(leaderboardEntity);
                    leaderboardEntity.getScoreEntityList().add(scoreEntity);
                    scoreEntity.setUser(user);
                    scoreEntity.setScore(0.0);
                    return this.userScoreRepository.save(scoreEntity);
                });
    }
}
