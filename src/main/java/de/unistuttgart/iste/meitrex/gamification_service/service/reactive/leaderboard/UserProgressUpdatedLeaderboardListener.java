package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.leaderboard;

import de.unistuttgart.iste.meitrex.gamification_service.events.internal.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentUserProgressUpdatedRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.LeaderboardEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.MoveLeaderboardGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ILeaderboardRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserScoreRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.functional.IScoringFunction;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements.IGoalProgressUpdater;
import de.unistuttgart.iste.meitrex.gamification_service.time.IPeriodCalculator;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import de.unistuttgart.iste.meitrex.gamification_service.time.Period;
import de.unistuttgart.iste.meitrex.generated.dto.Achievement;
import de.unistuttgart.iste.meitrex.generated.dto.Leaderboard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
public class UserProgressUpdatedLeaderboardListener extends AbstractInternalListener<PersistentUserProgressUpdatedEvent,  InternalUserProgressUpdatedEvent> {

    // Do not change to keep unique UUID even in case of refactoring.
    private static final String name = "LeaderboardListener";

    private static boolean isDayInRange(LocalDate day, LocalDate beginDate, LocalDate endDate) {
        return !day.isBefore(beginDate)  && day.isBefore(endDate);
    }

    public static Optional<Integer> computeRank(List<UserScoreEntity> scoreEntityList, UUID userID) {
        if(Objects.isNull(scoreEntityList) || Objects.isNull(userID)) {
            return Optional.empty();
        }
        List<UserScoreEntity> sortedScoreEntityList = scoreEntityList.stream()
                .sorted(Comparator.comparing(UserScoreEntity::getScore).reversed()).toList();
        int rank = 1;
        for(UserScoreEntity userScoreEntity : sortedScoreEntityList) {
            final UserEntity userEntity = userScoreEntity.getUser();
            if(Objects.nonNull(userEntity)
                    && userID.equals(userEntity.getId())) {
                return Optional.of(rank);
            }
            rank++;
        }
        return Optional.empty();
    }

    private static LeaderboardEntity extractMostRecentLeaderboardFromOrderedList(List<LeaderboardEntity> leaderboardList, Period period) {
        for(LeaderboardEntity leaderboardEntity : leaderboardList) {
            if(period.equals(leaderboardEntity.getPeriod())) {
                return leaderboardEntity;
            }
        }
        throw new IllegalStateException();
     }



    private final ITimeService timeService;

    private final ICourseCreator courseCreator;

    private final IUserCreator userCreator;

    private final IScoringFunction scoringFunction;

    private final IPeriodCalculator periodCalculator;

    private final ILeaderboardRepository leaderboardRepository;

    private final IUserScoreRepository userScoreRepository;

    private final IGoalProgressUpdater goalProgressUpdater;

    public UserProgressUpdatedLeaderboardListener(
            @Autowired  IPersistentUserProgressUpdatedRepository persistentEventRepository,
            @Autowired  IPersistentEventStatusRepository eventStatusRepository,
            @Autowired ITimeService timeService,
            @Autowired ICourseCreator courseCreator,
            @Autowired IUserCreator userCreator,
            @Autowired IScoringFunction scoringFunction,
            @Autowired IPeriodCalculator periodCalculator,
            @Autowired ILeaderboardRepository leaderboardRepository,
            @Autowired IUserScoreRepository userScoreRepository,
            @Autowired  IGoalProgressUpdater goalProgressUpdater
    ) {
        super(persistentEventRepository, eventStatusRepository, timeService);
        this.timeService = Objects.requireNonNull(timeService);
        this.courseCreator = Objects.requireNonNull(courseCreator);
        this.userCreator = Objects.requireNonNull(userCreator);
        this.scoringFunction = Objects.requireNonNull(scoringFunction);
        this.periodCalculator = Objects.requireNonNull(periodCalculator);
        this.leaderboardRepository = Objects.requireNonNull(leaderboardRepository);
        this.userScoreRepository = Objects.requireNonNull(userScoreRepository);
        this.goalProgressUpdater = Objects.requireNonNull(goalProgressUpdater);
    }

    @Override
    @EventListener
    public void process(InternalUserProgressUpdatedEvent internalEvent) {
        super.process(internalEvent);
    }

    @Override
    protected String getName() {
        return name;
    }


    @Override
    protected void doProcess(PersistentUserProgressUpdatedEvent internalEvent)
            throws TransientEventListenerException, NonTransientEventListenerException {
        final LocalDate today = this.timeService.toDate();
        final UserEntity userEntity = this.userCreator.fetchOrCreate(internalEvent.getUserId());
        final CourseEntity courseEntity = this.courseCreator.fetchOrCreate(internalEvent.getCourseId());


        final List<LeaderboardEntity> leaderboardEntityList = Arrays.stream(Period.values())
                .map(period ->  this.leaderboardRepository.findByCourseAndPeriodOrderByStartDateDesc(courseEntity, period))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        if(leaderboardEntityList.size() < Period.values().length || checkIfLeaderboardIsOutdated(leaderboardEntityList, today)) {
            throw new TransientEventListenerException();
        }

        this.updateUserScoreEntity(extractMostRecentLeaderboardFromOrderedList(leaderboardEntityList, Period.ALL_TIME), userEntity, courseEntity, internalEvent);
        this.updateUserScoreEntity(extractMostRecentLeaderboardFromOrderedList(leaderboardEntityList, Period.MONTHLY), userEntity, courseEntity, internalEvent);
        this.updateUserScoreEntity(extractMostRecentLeaderboardFromOrderedList(leaderboardEntityList, Period.WEEKLY), userEntity, courseEntity, internalEvent);

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

    private void updateUserScoreEntity(LeaderboardEntity leaderboardEntity, UserEntity userEntity, CourseEntity courseEntity, PersistentUserProgressUpdatedEvent event) {
        Optional<Integer> curRankOptional = computeRank(leaderboardEntity.getScoreEntityList(), userEntity.getId());
        final UserScoreEntity userScoreEntity = this.findOrCreateMostRecentUserScoreEntity(leaderboardEntity, userEntity, courseEntity, leaderboardEntity.getPeriod());
        final double curScore = userScoreEntity.getScore();
        final double newAdditionalScore = this.scoringFunction.score(event.getCorrectness(), event.getAssessmentAttempt());
        userScoreEntity.setScore(curScore + newAdditionalScore);
        Optional<Integer> newRankOptional = computeRank(leaderboardEntity.getScoreEntityList(), userEntity.getId());
        if(curRankOptional.isPresent() && newRankOptional.isPresent()) {
            int oldRank = curRankOptional.get();
            int newRank = newRankOptional.get();
            if(oldRank != newRank) {
                goalProgressUpdater.updateGoalProgressEntitiesForUser(userEntity, MoveLeaderboardGoalProgressEvent.builder()
                        .oldRank(oldRank)
                        .newRank(newRank)
                        .build()
                );
            }
        }
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
