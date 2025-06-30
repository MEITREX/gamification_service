package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "AnswerForumQuestionGoal")
@Data@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnswerForumQuestionGoalEntity extends CountableGoalEntity{
    public String generateDescription(){
        return "";
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
