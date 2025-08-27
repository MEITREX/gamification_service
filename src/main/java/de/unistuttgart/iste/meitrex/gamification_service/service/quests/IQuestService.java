package de.unistuttgart.iste.meitrex.gamification_service.service.quests;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestSetEntity;

import java.util.UUID;

public interface IQuestService {
    QuestSetEntity getDailyQuestSetForUser(final UUID courseId, final UUID userId);
}
