package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserCourseDataEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.HasGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseMembershipHandler;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements.IGoalProgressUpdater;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.*;

@Component
@Slf4j
@Transactional
class DefaultGoalProgressService implements IGoalProgressService {

    private static LoginStreakGoalProgressEvent getLoginStreakGoalProgressEvent(UUID userId, UUID courseId) {
        return LoginStreakGoalProgressEvent
                .builder()
                .userId(userId)
                .loginTime(OffsetDateTime.now())
                .build();
    }

    private static ReceiveItemsGoalProgressEvent getReceiveItemsGoalProgressEvent(UUID userId) {
        return ReceiveItemsGoalProgressEvent
                .builder()
                .userId(userId)
                .build();
    }

    private static LotteryRunGoalProgressEvent getLotteryRunGoalProgressEvent(UUID userId) {
        return LotteryRunGoalProgressEvent
                .builder()
                .userId(userId)
                .build();
    }

    private static EquipItemGoalProgressEvent getEquipItemGoalProgressEvent(UUID userId) {
        return EquipItemGoalProgressEvent
                .builder()
                .userId(userId)
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
        log.info("User {} logged in to course {}", userId, courseId);
        final UserEntity userEntity = userCreator.fetchOrCreate(userId);
        final CourseEntity courseEntity = courseCreator.fetchOrCreate(courseId);
        courseMembershipHandler.addUserToCourseIfNotAlready(courseEntity, userEntity);
        LoginStreakGoalProgressEvent loginStreakGoalProgressEvent = getLoginStreakGoalProgressEvent(userId, courseId);
        this.goalProgressUpdater.updateGoalProgressEntitiesForUser(userEntity, courseId, loginStreakGoalProgressEvent);
        return userId;
    }

    @Override
    public void itemReceivedProgress(UserEntity user) {
        log.info("item received for user {}", user.getId());
        ReceiveItemsGoalProgressEvent receiveItemsGoalProgressEvent = getReceiveItemsGoalProgressEvent(user.getId());
        this.goalProgressUpdater.updateGoalProgressEntitiesForUser(user, receiveItemsGoalProgressEvent);
    }

    @Override
    public void equipItemProgress(UserEntity user) {
        log.info("equip item progress for user {}", user.getId());
        EquipItemGoalProgressEvent equipItemGoalProgressEvent = getEquipItemGoalProgressEvent(user.getId());
        this.goalProgressUpdater.updateGoalProgressEntitiesForUser(user, equipItemGoalProgressEvent);
    }

    @Override
    public void lotteryRunProgress(UserEntity user) {
        log.info("lottery run progress for user {}", user.getId());
        LotteryRunGoalProgressEvent lotteryRunGoalProgressEvent = getLotteryRunGoalProgressEvent(user.getId());
        this.goalProgressUpdater.updateGoalProgressEntitiesForUser(user, lotteryRunGoalProgressEvent);
    }
}
