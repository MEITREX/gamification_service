package de.unistuttgart.iste.meitrex.gamification_service.service.internal.quests;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestEntity;

public interface IQuestCompletionHandler {
    void onQuestCompleted(QuestEntity quest, UserGoalProgressEntity goalProgressEntity);
}
