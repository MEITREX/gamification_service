package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.CombineUserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.CountableUserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;


@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity(name = "OrCombinatorGoal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrCombinatorGoalEntity extends GoalEntity{

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    GoalEntity goal1;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    GoalEntity goal2;

    public String generateDescription() {
        return goal1.generateDescription().substring(0, goal1.generateDescription().length() - 1)+ " and " +
                goal2.generateDescription().substring(0, 1).toLowerCase() + goal2.generateDescription().substring(1);
    }

    public void updateProgress(GoalProgressEvent progressEvent, UserGoalProgressEntity userGoalProgress) {
        if (userGoalProgress instanceof CombineUserGoalProgressEntity combineUserGoalProgress) {
            combineUserGoalProgress.getUserGoalProgressEntity1().updateProgress(progressEvent);
            combineUserGoalProgress.getUserGoalProgressEntity2().updateProgress(progressEvent);
            if (combineUserGoalProgress.getUserGoalProgressEntity1().isCompleted() || combineUserGoalProgress.getUserGoalProgressEntity2().isCompleted()){
                combineUserGoalProgress.setCompleted(true);
            }
        }
    }

    //@Override
    @Override
    public UserGoalProgressEntity generateUserGoalProgress(UserEntity user) {
        return new CombineUserGoalProgressEntity(user, this,
                goal1.generateUserGoalProgress(user),
                goal2.generateUserGoalProgress(user));
    }
}
