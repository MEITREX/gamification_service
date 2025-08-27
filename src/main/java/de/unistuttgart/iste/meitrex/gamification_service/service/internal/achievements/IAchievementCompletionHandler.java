package de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.*;



public interface IAchievementCompletionHandler {
    void onAchievementCompleted(AchievementEntity achievement, UserGoalProgressEntity goalProgressEntity);
}
