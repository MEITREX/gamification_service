package de.unistuttgart.iste.meitrex.gamification_service.service.quests.quest_generation;

import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestEntity;
import de.unistuttgart.iste.meitrex.gamification_service.quests.DailyQuestType;

import java.util.Optional;

public interface IQuestGenerator {
    Optional<QuestEntity> generateQuest(final CourseEntity courseEntity,
                                        final UserEntity userEntity) throws ContentServiceConnectionException;
    DailyQuestType generatesQuestType(); // Returns the type of quest this generator supports
}
