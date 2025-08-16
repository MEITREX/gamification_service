package de.unistuttgart.iste.meitrex.gamification_service.service.quests.quest_generation;

import de.unistuttgart.iste.meitrex.gamification_service.quests.DailyQuestType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class QuestGeneratorServiceFactory {
    private final Map<DailyQuestType, IQuestGenerator> questGenerators = new EnumMap<>(DailyQuestType.class);

    public QuestGeneratorServiceFactory(final List<IQuestGenerator> services) {
        for (final IQuestGenerator generator : services) {
            if (generator.generatesQuestType() != null) {
                questGenerators.put(generator.generatesQuestType(), generator);
            }
        }
    }

    public IQuestGenerator getQuestGenerator(final DailyQuestType questType) {
        return questGenerators.get(questType);
    }
}
