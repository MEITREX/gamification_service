package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.leaderboard;

import de.unistuttgart.iste.meitrex.gamification_service.events.internal.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentUserProgressUpdatedRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.LeaderboardEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ILeaderboardRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserScoreRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.functional.IScoringFunction;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.gamification_service.time.IPeriodCalculator;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import de.unistuttgart.iste.meitrex.gamification_service.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
class UserProgressUpdatedLeaderboardListener extends AbstractInternalListener<PersistentUserProgressUpdatedEvent,  InternalUserProgressUpdatedEvent> {

    // Do not change to keep unique UUID even in case of refactoring.
    private static final String name = "LeaderboardListener";

    private static boolean isDayInRange(LocalDate day, LocalDate beginDate, LocalDate endDate) {
        return !day.isBefore(beginDate)  && day.isBefore(endDate);
    }

    private final ITimeService timeService;

    private final ICourseCreator courseCreator;

    private final IUserCreator userCreator;

    private final IScoringFunction scoringFunction;

    private final IPeriodCalculator periodCalculator;

    private final ILeaderboardRepository leaderboardRepository;

    private final IUserScoreRepository userScoreRepository;


    public UserProgressUpdatedLeaderboardListener(
            @Autowired  IPersistentUserProgressUpdatedRepository persistentEventRepository,
            @Autowired  IPersistentEventStatusRepository eventStatusRepository,
            @Autowired ITimeService timeService,
            @Autowired ICourseCreator courseCreator,
            @Autowired IUserCreator userCreator,
            @Autowired IScoringFunction scoringFunction,
            @Autowired IPeriodCalculator periodCalculator,
            @Autowired ILeaderboardRepository leaderboardRepository,
            @Autowired IUserScoreRepository userScoreRepository
    ) {
        super(persistentEventRepository, eventStatusRepository, timeService);
        this.timeService = Objects.requireNonNull(timeService);
        this.courseCreator = Objects.requireNonNull(courseCreator);
        this.userCreator = Objects.requireNonNull(userCreator);
        this.scoringFunction = Objects.requireNonNull(scoringFunction);
        this.periodCalculator = Objects.requireNonNull(periodCalculator);
        this.leaderboardRepository = Objects.requireNonNull(leaderboardRepository);
        this.userScoreRepository = Objects.requireNonNull(userScoreRepository);
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
        this.updateUserScoreEntity(leaderboardEntityList, userEntity, courseEntity, internalEvent);

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
