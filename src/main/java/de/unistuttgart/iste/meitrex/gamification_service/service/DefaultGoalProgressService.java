package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserCourseDataEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.HasGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.LoginStreakGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseMembershipHandler;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements.IGoalProgressUpdater;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.*;

@Component
class DefaultGoalProgressService implements IGoalProgressService {

    private static LoginStreakGoalProgressEvent getLoginStreakGoalProgressEvent(UUID userId, UUID courseId) {
        return LoginStreakGoalProgressEvent
                .builder()
                .userId(userId)
                .loginTime(OffsetDateTime.now())
                .build();
    }

    private IUserCreator userCreator;

    private ICourseCreator courseCreator;

    private ICourseMembershipHandler courseMembershipHandler;

    private IGoalProgressUpdater goalProgressUpdater;

    public DefaultGoalProgressService(@Autowired  IUserCreator userCreator, @Autowired ICourseCreator courseCreator, @Autowired ICourseMembershipHandler courseMembershipHandler, @Autowired IGoalProgressUpdater goalProgressUpdater) {
        this.userCreator = Objects.requireNonNull(userCreator);
        this.courseCreator = Objects.requireNonNull(courseCreator);
        this.courseMembershipHandler = Objects.requireNonNull(courseMembershipHandler);
        this.goalProgressUpdater = Objects.requireNonNull(goalProgressUpdater);
    }

    @Override
    public UUID loginUser(UUID userId, UUID courseId) {
        final UserEntity userEntity = userCreator.fetchOrCreate(userId);
        final CourseEntity courseEntity = courseCreator.fetchOrCreate(courseId);
        courseMembershipHandler.addUserToCourseIfNotAlready(courseEntity, userEntity);
        LoginStreakGoalProgressEvent loginStreakGoalProgressEvent = getLoginStreakGoalProgressEvent(userId, courseId);
        this.goalProgressUpdater.updateGoalProgressEntitiesForUser(userEntity, courseId, loginStreakGoalProgressEvent);
        return userId;
    }
}
