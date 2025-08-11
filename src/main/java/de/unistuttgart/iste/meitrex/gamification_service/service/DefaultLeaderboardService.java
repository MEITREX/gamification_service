package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.events.internal.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.*;
import de.unistuttgart.iste.meitrex.gamification_service.exception.ResourceNotFoundException;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.LeaderboardEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper.LeaderboardMapper;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ICourseRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ILeaderboardRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserScoreRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.scoring.IScoringFunction;
import de.unistuttgart.iste.meitrex.gamification_service.time.IPeriodCalculator;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import de.unistuttgart.iste.meitrex.gamification_service.time.Period;
import de.unistuttgart.iste.meitrex.generated.dto.Leaderboard;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;


@Component
class DefaultLeaderboardService implements ILeaderboardService {

    private static final String ERR_MSG_NO_SUCH_PERSISTENT_USER_PROGRESS_UPDATED_EVENT
            = "Can not process user progress updated event since its persistent peer is missing.";

    private static final String ERR_MSG_ILLEGAL_ATTEMPT_OF_FUTURE_LEADERBOARD_CREATION
            = "Can not create a new leader board starting in the future.";


    private static void assureUpdateCourseLeaderboardPreconditionsAreMet(CourseEntity courseEntity, LeaderboardEntity leaderboardEntity, LocalDate now, Period period)  {
        Objects.requireNonNull(courseEntity);
        Objects.requireNonNull(leaderboardEntity);
        Objects.requireNonNull(now);
        Objects.requireNonNull(period);
        final LocalDate startDate = leaderboardEntity.getStartDate();
        if(now.isBefore(startDate)) {
            throw new IllegalArgumentException(ERR_MSG_ILLEGAL_ATTEMPT_OF_FUTURE_LEADERBOARD_CREATION);
        }
    }



    private static boolean isDayInRange(LocalDate day, LocalDate beginDate, LocalDate endDate) {
        return !day.isBefore(beginDate)  && day.isBefore(endDate);
    }

    private static LeaderboardEntity instantiateLeaderboard(
            CourseEntity courseEntity,
            LocalDate startDate,
            Period period
    ) {
        // Initializes attributes
        LeaderboardEntity newLeaderboard = new LeaderboardEntity();
        newLeaderboard.setTitle(String.format("%s - %s - %s", courseEntity.getTitle(), period, startDate));
        newLeaderboard.setStartDate(startDate);
        newLeaderboard.setPeriod(period);
        newLeaderboard.setCourse(courseEntity);
        // Updates references
        courseEntity.getLeaderboardEntityList().add(newLeaderboard);
        newLeaderboard.setCourse(courseEntity);
        return newLeaderboard;
    }

    // Service

    private final DefaultUserService userService;

    private final DefaultCourseService courseService;


    // Repositories

    private final IUserScoreRepository userScoreRepository;

    private final ILeaderboardRepository leaderboardRepository;

    private final ICourseRepository courseRepository;

    private final IPersistentUserProgressUpdatedRepository userProgressUpdatedRepository;

    //Mapping

    private final LeaderboardMapper leaderboardMapper;

    private final int dtoRecursionDepth;

    // Functional

    private final IScoringFunction scoringFunction;

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
            @Autowired ITimeService timeService,
            @Autowired LeaderboardMapper leaderboardMapper,
            @Value("${de.unistuttgart.iste.meitrex.gamification_service.service.dtoRecursionDepth: 3}")
            int dtoRecursionDepth
    ) {
        this.scoringFunction = Objects.requireNonNull(scoringFunction);
        this.userScoreRepository = Objects.requireNonNull(userScoreRepository);
        this.leaderboardRepository = Objects.requireNonNull(leaderboardRepository);
        this.courseRepository = Objects.requireNonNull(courseRepository);
        this.userService = Objects.requireNonNull(userService);
        this.courseService = Objects.requireNonNull(courseService);
        this.userProgressUpdatedRepository = Objects.requireNonNull(userProgressUpdatedRepository);
        this.periodCalculator = Objects.requireNonNull(periodCalculator);
        this.timeService = Objects.requireNonNull(timeService);
        this.leaderboardMapper = Objects.requireNonNull(leaderboardMapper);
        this.dtoRecursionDepth = dtoRecursionDepth;
    }

    // Interface implementation

    @Override
    public List<Leaderboard> find(UUID courseID, LocalDate date, Period period) {
        return this.leaderboardRepository.findByCourseIdAndDateAfterAndPeriod(courseID, date, period)
                .stream()
                .map(leaderboard -> this.leaderboardMapper.toDTO(leaderboard, dtoRecursionDepth))
                .toList();
    }



    // Scheduled logic for leaderboard creation and maintenance.

    @Transactional
    @Scheduled(cron = "0 0 0 * * MON")
    public void runWeeklyLeaderboardUpdate() {
        this.updateAllCourseLeaderboards(Period.WEEKLY);
    }

    @Transactional
    @Scheduled(cron = "0 0 0 1 * *")
    public void runMonthlyLeaderboardUpdate() {
        this.updateAllCourseLeaderboards(Period.MONTHLY);
    }

    private void updateAllCourseLeaderboards(Period period) {
        LocalDate localDate = this
                .timeService
                .toDate();
        this.courseRepository
                .findAll()
                .forEach(courseEntity -> this.updateCourseLeaderboard(courseEntity, localDate, period));
    }

    private void updateCourseLeaderboard(CourseEntity courseEntity, LocalDate now, Period period){
        this.leaderboardRepository
                .findByCourseAndPeriodOrderByStartDateDesc(courseEntity, period)
                .stream()
                .findFirst()
                .ifPresent(leaderboard -> this.updateCourseLeaderboard(courseEntity, leaderboard, now, Period.WEEKLY));
    }

    private void updateCourseLeaderboard(CourseEntity courseEntity, LeaderboardEntity leaderboardEntity,  LocalDate now, Period period) {
        assureUpdateCourseLeaderboardPreconditionsAreMet(courseEntity, leaderboardEntity, now, period);

        final LocalDate curStartDate = leaderboardEntity.getStartDate();
        final LocalDate nextStartDate = this.periodCalculator.calcSucStartDate(curStartDate, period);

        if(isDayInRange(now, curStartDate, nextStartDate)) {
            return;
        }

        LeaderboardEntity newLeaderboard = instantiateLeaderboard(courseEntity, nextStartDate, period);
        newLeaderboard = this.leaderboardRepository.save(newLeaderboard);
        updateCourseLeaderboard(courseEntity, newLeaderboard, now, period);
    }


    //Logic handling user progress

    @Transactional
    @EventListener
    public void onUserProgressUpdated(InternalUserProgressUpdatedEvent event) {
        final UUID id = Objects.requireNonNull(event.getId());
        final PersistentUserProgressUpdatedEvent persistentEvent
                = this.userProgressUpdatedRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ERR_MSG_NO_SUCH_PERSISTENT_USER_PROGRESS_UPDATED_EVENT));
        processPersistentUserProgressUpdatedEvent(persistentEvent);
    }

    @Transactional
    @Scheduled(fixedRate = 5000)
    public void retryFailedPersistentUserProgressUpdatedEventsProcessing() {
        this.userProgressUpdatedRepository
                .findByStatus(PersistentEvent.Status.FAILED_RETRY)
                .forEach(this::processPersistentUserProgressUpdatedEvent);
    }


    private void processPersistentUserProgressUpdatedEvent(PersistentUserProgressUpdatedEvent event) {

        final long now = this.timeService.curTime();
        final LocalDate today = this.timeService.toDate();

        final UserEntity userEntity = this.userService.fetchOrCreate(event.getUserId());
        final CourseEntity courseEntity = this.courseService.fetchOrCreate(event.getCourseId());

        this.leaderboardRepository.findByCourseAndPeriodOrderByStartDateDesc(courseEntity, Period.WEEKLY);

        final List<LeaderboardEntity> leaderboardEntityList = Arrays.stream(Period.values())
                .map(period ->  this.leaderboardRepository.findByCourseAndPeriodOrderByStartDateDesc(courseEntity, period))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();



        if(leaderboardEntityList.size() < Period.values().length || checkIfLeaderboardIsOutdated(leaderboardEntityList, today)) {
            this.handleUnmetDependency(event, now);
            return;
        }

        this.updateUserScoreEntity(leaderboardEntityList, userEntity, courseEntity, event);

        event.setStatus(PersistentEvent.Status.PROCESSED);

    }

    // Handle PersistentUserProgressUpdatedEvent in case of missing dependencies

    private boolean checkIfLeaderboardIsOutdated(List<LeaderboardEntity> leaderboardEntityList, LocalDate today) {
        for(LeaderboardEntity leaderboardEntity : leaderboardEntityList) {
            if(!Period.ALL_TIME.equals(leaderboardEntity.getPeriod())){
                final LocalDate curStartDate = leaderboardEntity.getStartDate();
                final LocalDate nextStartDate = this.periodCalculator.calcSucStartDate(curStartDate, leaderboardEntity.getPeriod());
                if(!isDayInRange(today, curStartDate, nextStartDate)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void handleUnmetDependency(PersistentUserProgressUpdatedEvent event, Long lastProcessingTimestamp) {
        int newCount = event.getAttemptCount() + 1;
        event.setAttemptCount(newCount);
        event.setLastProcessingAttemptTimestamp(lastProcessingTimestamp);
        PersistentEvent.Status newStatus = newCount >= event.getMaxCount() ? PersistentEvent.Status.FAILED : PersistentEvent.Status.FAILED_RETRY;
        event.setStatus(newStatus);
    }


    private void updateUserScoreEntity(List<LeaderboardEntity> leaderboardEntityList, UserEntity userEntity, CourseEntity courseEntity, PersistentUserProgressUpdatedEvent event) {
        leaderboardEntityList.forEach(leaderboardEntity -> {
            final UserScoreEntity userScoreEntity = this.findOrCreateMostRecentUserScoreEntity(leaderboardEntity, userEntity, courseEntity, leaderboardEntity.getPeriod());
            final double curScore = userScoreEntity.getScore();
            final double newAdditionalScore = this.scoringFunction.score(event.getCorrectness(), event.getAssessmentAttempt());
            userScoreEntity.setScore(curScore + newAdditionalScore);
        });
    }

    private UserScoreEntity findOrCreateMostRecentUserScoreEntity(LeaderboardEntity leaderboardEntity, UserEntity user, CourseEntity course, Period period) {
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
