package de.unistuttgart.iste.meitrex.gamification_service.service.internal.quests;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class DefaultQuestCompletionHandler implements IQuestCompletionHandler{

    public void onQuestCompleted(QuestEntity quest, UserGoalProgressEntity goalProgressEntity) {
        goalProgressEntity.getUser().getInventory().addPoints(quest.getRewardPoints());
    }
}
