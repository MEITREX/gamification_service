package de.unistuttgart.iste.meitrex.gamification_service.service.quests;

import de.unistuttgart.iste.meitrex.generated.dto.QuestSet;

import java.util.UUID;

public interface IQuestService {
    QuestSet getDailyQuestSetForUser(final UUID courseId, final UUID userId);
}
