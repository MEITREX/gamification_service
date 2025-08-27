package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.AnswerForumGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.CountableUserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Entity(name = "AnswerForumQuestionGoal")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnswerForumQuestionGoalEntity extends CountableGoalEntity {

    @Override
    public String generateDescription() {
        return "Answer " + super.getRequiredCount() + " questions in the Forum.";
    }

    @Override
    protected void populateFromOther(GoalEntity goal) {
    }

    @Override
    public boolean updateProgressInternal(GoalProgressEvent goalProgressEvent, UserGoalProgressEntity userGoalProgressEntity) {
        if (goalProgressEvent instanceof AnswerForumGoalProgressEvent) {
            if (userGoalProgressEntity instanceof CountableUserGoalProgressEntity countableUserGoalProgressEntity) {
                countableUserGoalProgressEntity.setCompletedCount(countableUserGoalProgressEntity.getCompletedCount() + 1);
                if (countableUserGoalProgressEntity.getCompletedCount() >= super.getRequiredCount()
                        && !countableUserGoalProgressEntity.isCompleted()) {
                    countableUserGoalProgressEntity.setCompleted(true);
                    return true;
                }
            }
        }

        return false;
    }
}
