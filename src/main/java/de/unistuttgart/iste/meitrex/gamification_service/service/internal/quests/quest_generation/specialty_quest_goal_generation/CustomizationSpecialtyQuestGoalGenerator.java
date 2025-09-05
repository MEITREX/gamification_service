package de.unistuttgart.iste.meitrex.gamification_service.service.internal.quests.quest_generation.specialty_quest_goal_generation;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.EquipItemGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CustomizationSpecialtyQuestGoalGenerator implements ISpecialtyQuestGoalGenerator {
    @Override
    public GamificationCategory getCategory() {
        return GamificationCategory.CUSTOMIZATION;
    }

    @Override
    public Optional<GoalEntity> generateGoal(UserEntity user, CourseEntity course) {
        EquipItemGoalEntity goal = new EquipItemGoalEntity();
        goal.setTrackingTimeToToday();
        return Optional.of(goal);
    }

    @Override
    public String getQuestTitle() {
        return "Put on some fresh clothes";
    }
}
