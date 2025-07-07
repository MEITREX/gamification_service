package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;


@Entity(name = "LoginStreakGoal")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginStreakGoalEntity extends CountableGoalEntity{
    public String generateDescription(){
        return "Login " + super.getRequiredCount() + " Days in a row.";
    }

    public void updateProgress(UserGoalProgressEntity userGoalProgressEntity){

    }

    public void updateProgress(UserGoalProgressEntity userGoalProgressEntity, OffsetDateTime loginTime){
        if (userGoalProgressEntity instanceof CountableUserGoalProgressEntity countableUserGoalProgressEntity) {
            countableUserGoalProgressEntity.updateProgress(loginTime);
        }
    }
}
