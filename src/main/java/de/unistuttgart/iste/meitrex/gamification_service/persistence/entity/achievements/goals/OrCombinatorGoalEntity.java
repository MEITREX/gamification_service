package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.CombineUserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;


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

    @Override
    public String generateDescription() {
        return goal1.generateDescription().substring(0, goal1.generateDescription().length() - 1)+ " and " +
                goal2.generateDescription().substring(0, 1).toLowerCase() + goal2.generateDescription().substring(1);
    }

    @Override
    protected void populateFromOther(GoalEntity goal) {
        if(!(goal instanceof OrCombinatorGoalEntity orGoal))
            throw new IllegalArgumentException("Passed goal needs to be of type OrCombinatorGoalEntity.");

        goal1 = orGoal.getGoal1().clone();
        goal2 = orGoal.getGoal2().clone();
    }

    @Override
    public boolean updateProgress(GoalProgressEvent progressEvent, UserGoalProgressEntity userGoalProgress) {
        if (userGoalProgress instanceof CombineUserGoalProgressEntity combineUserGoalProgress) {
            combineUserGoalProgress.getUserGoalProgressEntity1().updateProgress(progressEvent);
            combineUserGoalProgress.getUserGoalProgressEntity2().updateProgress(progressEvent);
            if ((combineUserGoalProgress.getUserGoalProgressEntity1().isCompleted()
                    || combineUserGoalProgress.getUserGoalProgressEntity2().isCompleted())
                    && !combineUserGoalProgress.isCompleted()){
                combineUserGoalProgress.setCompleted(true);
                return true;
            }
        }

        return false;
    }

    //@Override
    @Override
    public UserGoalProgressEntity generateUserGoalProgress(UserEntity user) {
        return new CombineUserGoalProgressEntity(user, this,
                goal1.generateUserGoalProgress(user),
                goal2.generateUserGoalProgress(user));
    }

    @Override
    public boolean equalsGoalTargets(GoalEntity other) {
        OrCombinatorGoalEntity otherOr = (OrCombinatorGoalEntity) other;
        return super.equalsGoalTargets(other)
                && goal1.equalsGoalTargets(otherOr.getGoal1())
                && goal2.equalsGoalTargets(otherOr.getGoal2());
    }
}
