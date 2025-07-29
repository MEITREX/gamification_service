package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.LoginStreakGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.CountableUserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Entity(name = "LoginStreakGoal")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginStreakGoalEntity extends CountableGoalEntity{

    @Override
    public String generateDescription(){
        return "Login " + super.getRequiredCount() + " Days in a row.";
    }

    @Override
    public boolean updateProgress(GoalProgressEvent goalProgressEvent, UserGoalProgressEntity userGoalProgressEntity){
        if (goalProgressEvent instanceof LoginStreakGoalProgressEvent loginStreakGoalProgressEvent) {
            if (userGoalProgressEntity instanceof CountableUserGoalProgressEntity countableUserGoalProgressEntity) {
                return countableUserGoalProgressEntity.updateProgress(loginStreakGoalProgressEvent.getLoginTime());
            }
        }
        return false;
    }
}
