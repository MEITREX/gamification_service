package de.unistuttgart.iste.meitrex.gamification_service.service.quests.quest_generation;

import de.unistuttgart.iste.meitrex.gamification_service.quests.DailyQuestType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class QuestGeneratorServiceFactory {

    private final Map<DailyQuestType, IDailyQuestGenerator> questGenerators = new EnumMap<>(DailyQuestType.class);

    public QuestGeneratorServiceFactory(final List<IDailyQuestGenerator> services) {
        for (final IDailyQuestGenerator generator : services) {
            if (generator.generatesQuestType() != null) {
                questGenerators.put(generator.generatesQuestType(), generator);
            }
        }
    }

    public IDailyQuestGenerator getQuestGenerator(final DailyQuestType questType) {
        return questGenerators.get(questType);
    }
}
