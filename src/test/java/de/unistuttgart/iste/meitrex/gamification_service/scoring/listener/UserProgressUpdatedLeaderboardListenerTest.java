package de.unistuttgart.iste.meitrex.gamification_service.scoring.listener;


import de.unistuttgart.iste.meitrex.gamification_service.events.internal.TransientEventListenerException;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentContentProgressedRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentUserProgressUpdatedRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.LeaderboardEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper.UserMapper;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ICourseRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ILeaderboardRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserScoreRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.functional.*;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements.IGoalProgressUpdater;
import de.unistuttgart.iste.meitrex.gamification_service.service.reactive.leaderboard.UserProgressUpdatedLeaderboardListener;
import de.unistuttgart.iste.meitrex.gamification_service.time.*;
import de.unistuttgart.iste.meitrex.generated.dto.Course;
import de.unistuttgart.iste.meitrex.generated.dto.Leaderboard;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;



public class UserProgressUpdatedLeaderboardListenerTest {

    private ITimeService timeService = new DefaultTimeService();

    private IPeriodCalculator periodCalculator = new DefaultPeriodCalculator(timeService);

    private IScoringFunction scoringFunction = new DefaultScoringFunction(-0.75F, 100);

    @Test
    public void testUserProgressByNewUser() {

        final UUID newUserID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UUID newCourseID =  UUID.fromString("00000000-0000-0000-0000-000000000000");

        final UserEntity newUserEntity =  UserEntity
                .builder()
                .id(newUserID)
                .xpValue(0)
                .userName("user")
                .build();
        final CourseEntity newCourseEntity = CourseEntity
                .builder()
                .id(newCourseID)
                .title("Kurs0")
                .leaderboardEntityList(new ArrayList<>())
                .build();

        final ICourseRepository courseRepository = mock(ICourseRepository.class);
        final IUserRepository userRepository = mock(IUserRepository.class);
        final IUserScoreRepository userScoreRepository = mock(IUserScoreRepository.class);
        final ILeaderboardRepository leaderboardRepository = mock(ILeaderboardRepository.class);
        when(courseRepository.findById(newCourseID)).thenReturn(Optional.of(newCourseEntity));
        when(userRepository.findById(newUserID)).thenReturn(Optional.of(newUserEntity));
        final long today = System.currentTimeMillis();
        for(Period period : Period.values()) {
            final LeaderboardEntity curPeriodLeaderboard = LeaderboardEntity
                    .builder()
                    .id(UUID.randomUUID())
                    .title("Leaderboard0")
                    .period(period)
                    .startDate(this.periodCalculator.calcStartDate(today, Period.ALL_TIME.equals(period) ? Period.MONTHLY : period))
                    .course(newCourseEntity)
                    .scoreEntityList(new ArrayList<>())
                    .build();
            final UserScoreEntity userScoreEntity = UserScoreEntity
                    .builder()
                    .user(newUserEntity)
                    .leaderboard(curPeriodLeaderboard)
                    .score(0.0D)
                    .build();
            newUserEntity.getLeaderboardList().add(userScoreEntity);
            curPeriodLeaderboard.getScoreEntityList().add(userScoreEntity);
            when(leaderboardRepository.findByCourseAndPeriodOrderByStartDateDesc(newCourseEntity, period)).thenReturn(Optional.of(curPeriodLeaderboard));
            when(userScoreRepository.findMostRecentUserScore(newUserEntity, newCourseEntity, period)).thenReturn(Optional.of(userScoreEntity));
        }

        final ICourseCreator courseCreator = mock(ICourseCreator.class);
        when(courseCreator.fetchOrCreate(newCourseID)).thenReturn(newCourseEntity);

        final IUserCreator userCreator = mock(IUserCreator.class);
        when(userCreator.fetchOrCreate(newUserID)).thenReturn(newUserEntity);

        final IGoalProgressUpdater goalProgressUpdater = mock(IGoalProgressUpdater.class);

        final UUID eventID = UUID.randomUUID();

        final PersistentUserProgressUpdatedEvent event = PersistentUserProgressUpdatedEvent.builder()
                .uuid(eventID)
                .userId(newUserID)
                .contentId(UUID.randomUUID())
                .courseId(newCourseID)
                .correctness(1.0)
                .assessmentAttempt(1)
                .build();
        final IPersistentUserProgressUpdatedRepository eventRepository = mock(IPersistentUserProgressUpdatedRepository.class);
        when(eventRepository.findById(eventID)).thenReturn(Optional.of(event));
        final IPersistentEventStatusRepository statusRepository = mock(IPersistentEventStatusRepository.class);

        final UserProgressUpdatedLeaderboardListener listener
                = new UserProgressUpdatedLeaderboardListener(eventRepository, statusRepository, timeService, courseCreator, userCreator, scoringFunction, periodCalculator, leaderboardRepository, userScoreRepository, goalProgressUpdater);
        listener.doProcess(event);
        for(UserScoreEntity userScoreEntity : newUserEntity.getLeaderboardList()) {
            assertEquals(59.0, userScoreEntity.getScore(), .01);
        }
    }

    @Test
    public void testUserProgressByExperiencedUser() {

        final UUID newUserID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UUID newCourseID =  UUID.fromString("00000000-0000-0000-0000-000000000000");

        final UserEntity newUserEntity =  UserEntity
                .builder()
                .id(newUserID)
                .xpValue(0)
                .userName("user")
                .build();
        final CourseEntity newCourseEntity = CourseEntity
                .builder()
                .id(newCourseID)
                .title("Kurs0")
                .leaderboardEntityList(new ArrayList<>())
                .build();

        final ICourseRepository courseRepository = mock(ICourseRepository.class);
        final IUserRepository userRepository = mock(IUserRepository.class);
        final IUserScoreRepository userScoreRepository = mock(IUserScoreRepository.class);
        final ILeaderboardRepository leaderboardRepository = mock(ILeaderboardRepository.class);
        when(courseRepository.findById(newCourseID)).thenReturn(Optional.of(newCourseEntity));
        when(userRepository.findById(newUserID)).thenReturn(Optional.of(newUserEntity));
        final long today = System.currentTimeMillis();
        for(Period period : Period.values()) {
            final LeaderboardEntity curPeriodLeaderboard = LeaderboardEntity
                    .builder()
                    .id(UUID.randomUUID())
                    .title("Leaderboard0")
                    .period(period)
                    .startDate(this.periodCalculator.calcStartDate(today, Period.ALL_TIME.equals(period) ? Period.MONTHLY : period))
                    .course(newCourseEntity)
                    .scoreEntityList(new ArrayList<>())
                    .build();
            final UserScoreEntity userScoreEntity = UserScoreEntity
                    .builder()
                    .user(newUserEntity)
                    .leaderboard(curPeriodLeaderboard)
                    .score(20D)
                    .build();
            newUserEntity.getLeaderboardList().add(userScoreEntity);
            curPeriodLeaderboard.getScoreEntityList().add(userScoreEntity);
            when(leaderboardRepository.findByCourseAndPeriodOrderByStartDateDesc(newCourseEntity, period)).thenReturn(Optional.of(curPeriodLeaderboard));
            when(userScoreRepository.findMostRecentUserScore(newUserEntity, newCourseEntity, period)).thenReturn(Optional.of(userScoreEntity));
        }

        final ICourseCreator courseCreator = mock(ICourseCreator.class);
        when(courseCreator.fetchOrCreate(newCourseID)).thenReturn(newCourseEntity);

        final IUserCreator userCreator = mock(IUserCreator.class);
        when(userCreator.fetchOrCreate(newUserID)).thenReturn(newUserEntity);

        final IGoalProgressUpdater goalProgressUpdater = mock(IGoalProgressUpdater.class);

        final UUID eventID = UUID.randomUUID();

        final PersistentUserProgressUpdatedEvent event = PersistentUserProgressUpdatedEvent.builder()
                .uuid(eventID)
                .userId(newUserID)
                .contentId(UUID.randomUUID())
                .courseId(newCourseID)
                .correctness(1.0)
                .assessmentAttempt(1)
                .build();
        final IPersistentUserProgressUpdatedRepository eventRepository = mock(IPersistentUserProgressUpdatedRepository.class);
        when(eventRepository.findById(eventID)).thenReturn(Optional.of(event));
        final IPersistentEventStatusRepository statusRepository = mock(IPersistentEventStatusRepository.class);

        final UserProgressUpdatedLeaderboardListener listener
                = new UserProgressUpdatedLeaderboardListener(eventRepository, statusRepository, timeService, courseCreator, userCreator, scoringFunction, periodCalculator, leaderboardRepository, userScoreRepository, goalProgressUpdater);
        listener.doProcess(event);
        for(UserScoreEntity userScoreEntity : newUserEntity.getLeaderboardList()) {
            assertEquals(79.0, userScoreEntity.getScore(), .01);
        }
    }

    @Test
    public void testPartialUserProgressByNewUser() {

        final UUID newUserID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UUID newCourseID =  UUID.fromString("00000000-0000-0000-0000-000000000000");

        final UserEntity newUserEntity =  UserEntity
                .builder()
                .id(newUserID)
                .xpValue(0)
                .userName("user")
                .build();
        final CourseEntity newCourseEntity = CourseEntity
                .builder()
                .id(newCourseID)
                .title("Kurs0")
                .leaderboardEntityList(new ArrayList<>())
                .build();

        final ICourseRepository courseRepository = mock(ICourseRepository.class);
        final IUserRepository userRepository = mock(IUserRepository.class);
        final IUserScoreRepository userScoreRepository = mock(IUserScoreRepository.class);
        final ILeaderboardRepository leaderboardRepository = mock(ILeaderboardRepository.class);
        when(courseRepository.findById(newCourseID)).thenReturn(Optional.of(newCourseEntity));
        when(userRepository.findById(newUserID)).thenReturn(Optional.of(newUserEntity));
        final long today = System.currentTimeMillis();
        for(Period period : Period.values()) {
            final LeaderboardEntity curPeriodLeaderboard = LeaderboardEntity
                    .builder()
                    .id(UUID.randomUUID())
                    .title("Leaderboard0")
                    .period(period)
                    .startDate(this.periodCalculator.calcStartDate(today, Period.ALL_TIME.equals(period) ? Period.MONTHLY : period))
                    .course(newCourseEntity)
                    .scoreEntityList(new ArrayList<>())
                    .build();
            final UserScoreEntity userScoreEntity = UserScoreEntity
                    .builder()
                    .user(newUserEntity)
                    .leaderboard(curPeriodLeaderboard)
                    .score(0.0D)
                    .build();
            newUserEntity.getLeaderboardList().add(userScoreEntity);
            curPeriodLeaderboard.getScoreEntityList().add(userScoreEntity);
            when(leaderboardRepository.findByCourseAndPeriodOrderByStartDateDesc(newCourseEntity, period)).thenReturn(Optional.of(curPeriodLeaderboard));
            when(userScoreRepository.findMostRecentUserScore(newUserEntity, newCourseEntity, period)).thenReturn(Optional.of(userScoreEntity));
        }

        final ICourseCreator courseCreator = mock(ICourseCreator.class);
        when(courseCreator.fetchOrCreate(newCourseID)).thenReturn(newCourseEntity);

        final IUserCreator userCreator = mock(IUserCreator.class);
        when(userCreator.fetchOrCreate(newUserID)).thenReturn(newUserEntity);

        final IGoalProgressUpdater goalProgressUpdater = mock(IGoalProgressUpdater.class);

        final UUID eventID = UUID.randomUUID();

        final PersistentUserProgressUpdatedEvent event = PersistentUserProgressUpdatedEvent.builder()
                .uuid(eventID)
                .userId(newUserID)
                .contentId(UUID.randomUUID())
                .courseId(newCourseID)
                .correctness(.5)
                .assessmentAttempt(1)
                .build();
        final IPersistentUserProgressUpdatedRepository eventRepository = mock(IPersistentUserProgressUpdatedRepository.class);
        when(eventRepository.findById(eventID)).thenReturn(Optional.of(event));
        final IPersistentEventStatusRepository statusRepository = mock(IPersistentEventStatusRepository.class);

        final UserProgressUpdatedLeaderboardListener listener
                = new UserProgressUpdatedLeaderboardListener(eventRepository, statusRepository, timeService, courseCreator, userCreator, scoringFunction, periodCalculator, leaderboardRepository, userScoreRepository, goalProgressUpdater);
        listener.doProcess(event);
        for(UserScoreEntity userScoreEntity : newUserEntity.getLeaderboardList()) {
            assertEquals(30.0, userScoreEntity.getScore(), .01);
        }
    }

    @Test
    public void testPartialUserProgressByExperiencedUser() {

        final UUID newUserID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UUID newCourseID =  UUID.fromString("00000000-0000-0000-0000-000000000000");

        final UserEntity newUserEntity =  UserEntity
                .builder()
                .id(newUserID)
                .xpValue(0)
                .userName("user")
                .build();
        final CourseEntity newCourseEntity = CourseEntity
                .builder()
                .id(newCourseID)
                .title("Kurs0")
                .leaderboardEntityList(new ArrayList<>())
                .build();

        final ICourseRepository courseRepository = mock(ICourseRepository.class);
        final IUserRepository userRepository = mock(IUserRepository.class);
        final IUserScoreRepository userScoreRepository = mock(IUserScoreRepository.class);
        final ILeaderboardRepository leaderboardRepository = mock(ILeaderboardRepository.class);
        when(courseRepository.findById(newCourseID)).thenReturn(Optional.of(newCourseEntity));
        when(userRepository.findById(newUserID)).thenReturn(Optional.of(newUserEntity));
        final long today = System.currentTimeMillis();
        for(Period period : Period.values()) {
            final LeaderboardEntity curPeriodLeaderboard = LeaderboardEntity
                    .builder()
                    .id(UUID.randomUUID())
                    .title("Leaderboard0")
                    .period(period)
                    .startDate(this.periodCalculator.calcStartDate(today, Period.ALL_TIME.equals(period) ? Period.MONTHLY : period))
                    .course(newCourseEntity)
                    .scoreEntityList(new ArrayList<>())
                    .build();
            final UserScoreEntity userScoreEntity = UserScoreEntity
                    .builder()
                    .user(newUserEntity)
                    .leaderboard(curPeriodLeaderboard)
                    .score(20D)
                    .build();
            newUserEntity.getLeaderboardList().add(userScoreEntity);
            curPeriodLeaderboard.getScoreEntityList().add(userScoreEntity);
            when(leaderboardRepository.findByCourseAndPeriodOrderByStartDateDesc(newCourseEntity, period)).thenReturn(Optional.of(curPeriodLeaderboard));
            when(userScoreRepository.findMostRecentUserScore(newUserEntity, newCourseEntity, period)).thenReturn(Optional.of(userScoreEntity));
        }

        final ICourseCreator courseCreator = mock(ICourseCreator.class);
        when(courseCreator.fetchOrCreate(newCourseID)).thenReturn(newCourseEntity);

        final IUserCreator userCreator = mock(IUserCreator.class);
        when(userCreator.fetchOrCreate(newUserID)).thenReturn(newUserEntity);

        final IGoalProgressUpdater goalProgressUpdater = mock(IGoalProgressUpdater.class);

        final UUID eventID = UUID.randomUUID();

        final PersistentUserProgressUpdatedEvent event = PersistentUserProgressUpdatedEvent.builder()
                .uuid(eventID)
                .userId(newUserID)
                .contentId(UUID.randomUUID())
                .courseId(newCourseID)
                .correctness(.5)
                .assessmentAttempt(1)
                .build();
        final IPersistentUserProgressUpdatedRepository eventRepository = mock(IPersistentUserProgressUpdatedRepository.class);
        when(eventRepository.findById(eventID)).thenReturn(Optional.of(event));
        final IPersistentEventStatusRepository statusRepository = mock(IPersistentEventStatusRepository.class);

        final UserProgressUpdatedLeaderboardListener listener
                = new UserProgressUpdatedLeaderboardListener(eventRepository, statusRepository, timeService, courseCreator, userCreator, scoringFunction, periodCalculator, leaderboardRepository, userScoreRepository, goalProgressUpdater);
        listener.doProcess(event);
        for(UserScoreEntity userScoreEntity : newUserEntity.getLeaderboardList()) {
            assertEquals(50.0, userScoreEntity.getScore(), .01);
        }
    }

    @Test
    public void testIncompleteLeaderboards() {

        final UUID newUserID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UUID newCourseID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UserEntity newUserEntity =  UserEntity
                .builder()
                .id(newUserID)
                .xpValue(0)
                .userName("user")
                .build();
        final CourseEntity newCourseEntity = CourseEntity
                .builder()
                .id(newCourseID)
                .title("Kurs0")
                .leaderboardEntityList(new ArrayList<>())
                .build();
        final ICourseRepository courseRepository = mock(ICourseRepository.class);
        final IUserRepository userRepository = mock(IUserRepository.class);
        final IUserScoreRepository userScoreRepository = mock(IUserScoreRepository.class);
        final ILeaderboardRepository leaderboardRepository = mock(ILeaderboardRepository.class);
        when(courseRepository.findById(newCourseID)).thenReturn(Optional.of(newCourseEntity));
        when(userRepository.findById(newUserID)).thenReturn(Optional.of(newUserEntity));
        final long today = System.currentTimeMillis();
        for(Period period : Arrays.asList(Period.values()).subList(0, 2)) {
            final LeaderboardEntity curPeriodLeaderboard = LeaderboardEntity
                    .builder()
                    .id(UUID.randomUUID())
                    .title("Leaderboard0")
                    .period(period)
                    .startDate(this.periodCalculator.calcStartDate(today, Period.ALL_TIME.equals(period) ? Period.MONTHLY : period))
                    .course(newCourseEntity)
                    .scoreEntityList(new ArrayList<>())
                    .build();
            final UserScoreEntity userScoreEntity = UserScoreEntity
                    .builder()
                    .user(newUserEntity)
                    .leaderboard(curPeriodLeaderboard)
                    .score(20D)
                    .build();
            newUserEntity.getLeaderboardList().add(userScoreEntity);
            curPeriodLeaderboard.getScoreEntityList().add(userScoreEntity);
            when(leaderboardRepository.findByCourseAndPeriodOrderByStartDateDesc(newCourseEntity, period)).thenReturn(Optional.of(curPeriodLeaderboard));
            when(userScoreRepository.findMostRecentUserScore(newUserEntity, newCourseEntity, period)).thenReturn(Optional.of(userScoreEntity));
        }

        final ICourseCreator courseCreator = mock(ICourseCreator.class);
        when(courseCreator.fetchOrCreate(newCourseID)).thenReturn(newCourseEntity);

        final IUserCreator userCreator = mock(IUserCreator.class);
        when(userCreator.fetchOrCreate(newUserID)).thenReturn(newUserEntity);

        final IGoalProgressUpdater goalProgressUpdater = mock(IGoalProgressUpdater.class);

        final UUID eventID = UUID.randomUUID();

        final PersistentUserProgressUpdatedEvent event = PersistentUserProgressUpdatedEvent.builder()
                .uuid(eventID)
                .userId(newUserID)
                .contentId(UUID.randomUUID())
                .courseId(newCourseID)
                .correctness(.5)
                .assessmentAttempt(1)
                .build();
        final IPersistentUserProgressUpdatedRepository eventRepository = mock(IPersistentUserProgressUpdatedRepository.class);
        when(eventRepository.findById(eventID)).thenReturn(Optional.of(event));
        final IPersistentEventStatusRepository statusRepository = mock(IPersistentEventStatusRepository.class);

        final UserProgressUpdatedLeaderboardListener listener
                = new UserProgressUpdatedLeaderboardListener(eventRepository, statusRepository, timeService, courseCreator, userCreator, scoringFunction, periodCalculator, leaderboardRepository, userScoreRepository, goalProgressUpdater);

        assertThrows(TransientEventListenerException.class, ()-> listener.doProcess(event));
    }

    @Test
    public void testIncompleteUserScores() {

        final UUID newUserID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UUID newCourseID =  UUID.fromString("00000000-0000-0000-0000-000000000000");

        final UserEntity newUserEntity =  UserEntity
                .builder()
                .id(newUserID)
                .xpValue(0)
                .userName("user")
                .build();
        final CourseEntity newCourseEntity = CourseEntity
                .builder()
                .id(newCourseID)
                .title("Kurs0")
                .leaderboardEntityList(new ArrayList<>())
                .build();

        final ICourseRepository courseRepository = mock(ICourseRepository.class);
        final IUserRepository userRepository = mock(IUserRepository.class);
        final IUserScoreRepository userScoreRepository = mock(IUserScoreRepository.class);
        final ILeaderboardRepository leaderboardRepository = mock(ILeaderboardRepository.class);
        when(courseRepository.findById(newCourseID)).thenReturn(Optional.of(newCourseEntity));
        when(userRepository.findById(newUserID)).thenReturn(Optional.of(newUserEntity));
        final long today = System.currentTimeMillis();
        for(Period period : Period.values()) {
            final LeaderboardEntity curPeriodLeaderboard = LeaderboardEntity
                    .builder()
                    .id(UUID.randomUUID())
                    .title("Leaderboard0")
                    .period(period)
                    .startDate(this.periodCalculator.calcStartDate(today, Period.ALL_TIME.equals(period) ? Period.MONTHLY : period))
                    .course(newCourseEntity)
                    .scoreEntityList(new ArrayList<>())
                    .build();
            when(leaderboardRepository.findByCourseAndPeriodOrderByStartDateDesc(newCourseEntity, period)).thenReturn(Optional.of(curPeriodLeaderboard));
            when(userScoreRepository.save(any(UserScoreEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        }

        final ICourseCreator courseCreator = mock(ICourseCreator.class);
        when(courseCreator.fetchOrCreate(newCourseID)).thenReturn(newCourseEntity);

        final IUserCreator userCreator = mock(IUserCreator.class);
        when(userCreator.fetchOrCreate(newUserID)).thenReturn(newUserEntity);

        final IGoalProgressUpdater goalProgressUpdater = mock(IGoalProgressUpdater.class);

        final UUID eventID = UUID.randomUUID();

        final PersistentUserProgressUpdatedEvent event = PersistentUserProgressUpdatedEvent.builder()
                .uuid(eventID)
                .userId(newUserID)
                .contentId(UUID.randomUUID())
                .courseId(newCourseID)
                .correctness(1.0)
                .assessmentAttempt(1)
                .build();
        final IPersistentUserProgressUpdatedRepository eventRepository = mock(IPersistentUserProgressUpdatedRepository.class);
        when(eventRepository.findById(eventID)).thenReturn(Optional.of(event));
        final IPersistentEventStatusRepository statusRepository = mock(IPersistentEventStatusRepository.class);

        final UserProgressUpdatedLeaderboardListener listener
                = new UserProgressUpdatedLeaderboardListener(eventRepository, statusRepository, timeService, courseCreator, userCreator, scoringFunction, periodCalculator, leaderboardRepository, userScoreRepository, goalProgressUpdater);

        listener.doProcess(event);
        for(UserScoreEntity userScoreEntity : newUserEntity.getLeaderboardList()) {
            assertEquals(59.0, userScoreEntity.getScore(), .01);
        }
    }
}

