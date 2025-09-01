package de.unistuttgart.iste.meitrex.gamification_service.service.internal.quests.quest_generation.specialty_quest_goal_generation;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;

import java.util.Optional;

public interface ISpecialtyQuestGoalGenerator {
    GamificationCategory getCategory();
    Optional<GoalEntity> generateGoal(UserEntity user, CourseEntity course);
    String getQuestTitle();
}
