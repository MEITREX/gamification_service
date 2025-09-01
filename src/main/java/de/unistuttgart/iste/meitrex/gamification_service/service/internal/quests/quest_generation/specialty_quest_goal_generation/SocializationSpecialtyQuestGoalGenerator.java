package de.unistuttgart.iste.meitrex.gamification_service.service.internal.quests.quest_generation.specialty_quest_goal_generation;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.MoveUpLeaderboardGoalEntity;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SocializationSpecialtyQuestGoalGenerator implements ISpecialtyQuestGoalGenerator {
    @Override
    public GamificationCategory getCategory() {
        return GamificationCategory.SOCIALIZATION;
    }

    @Override
    public Optional<GoalEntity> generateGoal(UserEntity user, CourseEntity course) {
        // TODO: Should check if user at top of leaderboard already before generating this
        MoveUpLeaderboardGoalEntity goal = new MoveUpLeaderboardGoalEntity();
        goal.setTrackingTimeToToday();
        return Optional.of(goal);
    }

    @Override
    public String getQuestTitle() {
        return "Socialization Quest";
    }
}
