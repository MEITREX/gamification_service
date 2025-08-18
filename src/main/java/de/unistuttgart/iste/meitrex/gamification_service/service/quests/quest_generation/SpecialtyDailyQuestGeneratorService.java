package de.unistuttgart.iste.meitrex.gamification_service.service.quests.quest_generation;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestEntity;
import de.unistuttgart.iste.meitrex.gamification_service.quests.DailyQuestType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpecialtyDailyQuestGeneratorService implements IQuestGenerator {
    @Override
    public Optional<QuestEntity> generateQuest(CourseEntity courseEntity,
                                               UserEntity userEntity,
                                               List<QuestEntity> otherQuests) {
        return Optional.empty();
    }

    @Override
    public DailyQuestType generatesQuestType() {
        return DailyQuestType.SPECIALTY;
    }
}
