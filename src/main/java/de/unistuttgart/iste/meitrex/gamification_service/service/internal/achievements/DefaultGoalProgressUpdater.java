package de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.HasGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
class DefaultGoalProgressUpdater implements IGoalProgressUpdater {

    private final IAchievementCompletionHandler achievementCompletionHandler;

    public DefaultGoalProgressUpdater(@Autowired IAchievementCompletionHandler achievementCompletionHandler) {
        this.achievementCompletionHandler = Objects.requireNonNull(achievementCompletionHandler);
    }

    public void updateGoalProgressEntitiesForUser(UserEntity user, UUID courseId, GoalProgressEvent goalProgressEvent) {
        List<UserGoalProgressEntity> completedGoals = user.getCourseData(courseId)
                .orElseThrow(() -> new IllegalArgumentException("updateGoalProgressEntitiesForUser(): User is not enrolled in course: " + courseId))
                .getGoalProgressEntities().stream()
                .filter(goalProgressEntity -> goalProgressEntity.updateProgress(goalProgressEvent))
                .toList();
        completedGoals.forEach(this::onGoalCompleted);
    }

    private void onGoalCompleted(UserGoalProgressEntity goalProgressEntity) {
        HasGoalEntity hasGoalEntity = Hibernate.unproxy(goalProgressEntity.getGoal().getParentWithGoal(), HasGoalEntity.class);
        if (hasGoalEntity instanceof AchievementEntity achievement) {
             achievementCompletionHandler.onAchievementCompleted(achievement, goalProgressEntity);
        }
    }

    public void updateGoalProgressEntitiesForUser(UserEntity user, GoalProgressEvent goalProgressEvent) {
        List<UserGoalProgressEntity> completedGoals = user.getCourseData()
                .stream()
                .flatMap(userCourseDataEntity -> userCourseDataEntity.getGoalProgressEntities().stream())
                .filter(goalProgressEntity -> goalProgressEntity.updateProgress(goalProgressEvent))
                .toList();
        completedGoals.forEach(this::onGoalCompleted);
    }
}
