package de.unistuttgart.iste.meitrex.gamification_service.service.internal.quests.quest_generation.specialty_quest_goal_generation;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.AskTutorCourseQuestionsGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AssistanceSpecialtyQuestGoalGenerator implements ISpecialtyQuestGoalGenerator{
    @Override
    public GamificationCategory getCategory() {
        return GamificationCategory.ASSISTANCE;
    }

    @Override
    public Optional<GoalEntity> generateGoal(UserEntity user, CourseEntity course) {
        AskTutorCourseQuestionsGoalEntity askTutorCourseQuestionsGoalEntity = new AskTutorCourseQuestionsGoalEntity();
        askTutorCourseQuestionsGoalEntity.setRequiredCount(1);
        askTutorCourseQuestionsGoalEntity.setTrackingTimeToToday();
        return Optional.of(askTutorCourseQuestionsGoalEntity);
    }

    @Override
    public String getQuestTitle() {
        return "Stop it! Get some help!";
    }
}
