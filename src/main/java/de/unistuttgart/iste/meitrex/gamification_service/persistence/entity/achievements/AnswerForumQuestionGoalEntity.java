package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Entity(name = "AnswerForumQuestionGoal")
@Data@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnswerForumQuestionGoalEntity extends CountableGoalEntity{
    public String generateDescription(){
        return "Answer " + super.getRequiredCount() + " questions in the Forum.";
    }

    public void updateProgress(UserGoalProgressEntity userGoalProgressEntity){
        if (userGoalProgressEntity instanceof CountableUserGoalProgressEntity countableUserGoalProgressEntity){
            countableUserGoalProgressEntity.setCompletedCount(countableUserGoalProgressEntity.getCompletedCount() + 1);
            if (countableUserGoalProgressEntity.getCompletedCount() >= super.getRequiredCount()) {
                countableUserGoalProgressEntity.setCompleted(true);
            }
        }
    }
}
