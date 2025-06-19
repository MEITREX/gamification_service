package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "CompletedQuizzesGoal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompletedQuizzesGoalEntity extends CountableGoalEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column
    float minimumScore;

    public String generateDescription(){
        return "";
    }

    public void updateProgress(UserGoalProgressEntity userGoalProgressEntity){
        if (userGoalProgressEntity instanceof CountableUserGoalProgressEntity countableUserGoalProgressEntity){
            if (countableUserGoalProgressEntity.getCompletedCount()>= getRequiredCount()) {
                countableUserGoalProgressEntity.setCompleted(true);
            }
        }
    }

    public void updateProgress(UserGoalProgressEntity userGoalProgressEntity, float score, UUID contentId){
        if (userGoalProgressEntity instanceof CountableUserGoalProgressEntity countableUserGoalProgressEntity){
            if (score >= minimumScore && !countableUserGoalProgressEntity.getContentIds().contains(contentId)) {
                countableUserGoalProgressEntity.setCompletedCount(countableUserGoalProgressEntity.getCompletedCount() + 1);
                countableUserGoalProgressEntity.getContentIds().add(contentId);
            }
        }
        updateProgress(userGoalProgressEntity);
    }
}
