package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.common.event.TutorCategory;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.AskedTutorAQuestionGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.CountableUserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import jakarta.persistence.Entity;

@Entity(name = "AskTutorCourseQuestionsGoal")
public class AskTutorCourseQuestionsGoalEntity extends CountableGoalEntity {
    @Override
    public String generateDescription() {
        if(super.getRequiredCount() == 1) {
            return "Ask the tutor a question related to the course.";
        } else {
            return "Ask the tutor " + super.getRequiredCount() + " questions related to the course.";
        }
    }

    @Override
    protected void populateFromOther(GoalEntity goal) {

    }

    @Override
    protected boolean updateProgressInternal(GoalProgressEvent goalProgressEvent, UserGoalProgressEntity userGoalProgress) {
        if(goalProgressEvent instanceof AskedTutorAQuestionGoalProgressEvent) {
            if(userGoalProgress instanceof CountableUserGoalProgressEntity countableUserGoalProgress) {
                countableUserGoalProgress.incrementCompletedCount();
                if(countableUserGoalProgress.getCompletedCount() >= super.getRequiredCount()
                        && !countableUserGoalProgress.isCompleted()) {
                    countableUserGoalProgress.setCompleted(true);
                    return true;
                }
            }
        }
        return false;
    }
}
