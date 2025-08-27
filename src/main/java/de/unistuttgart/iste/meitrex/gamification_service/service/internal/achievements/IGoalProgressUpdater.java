package de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;

import java.util.UUID;

public interface IGoalProgressUpdater {
    void updateGoalProgressEntitiesForUser(UserEntity user, UUID courseId, GoalProgressEvent goalProgressEvent);
}
