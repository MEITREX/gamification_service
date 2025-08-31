package de.unistuttgart.iste.meitrex.gamification_service.service.quests.quest_generation.specialty_quest_goal_generation;

import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class SpecialtyQuestGoalGeneratorFactory {
    private final Map<GamificationCategory, ISpecialtyQuestGoalGenerator> goalGenerators
            = new EnumMap<>(GamificationCategory.class);

    public SpecialtyQuestGoalGeneratorFactory(final List<ISpecialtyQuestGoalGenerator> services) {
        for (final ISpecialtyQuestGoalGenerator generator : services) {
            if (generator.getCategory() != null) {
                goalGenerators.put(generator.getCategory(), generator);
            }
        }
    }

    public ISpecialtyQuestGoalGenerator getGoalGenerator(final GamificationCategory category) {
        return goalGenerators.get(category);
    }
}
