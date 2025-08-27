package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.MoveLeaderboardGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@AllArgsConstructor
public class MoveUpLeaderboardGoalEntity extends GoalEntity {

    @Override
    public String generateDescription() {
        return "Move up a rank in any leaderboard of this course.";
    }

    @Override
    protected void populateFromOther(GoalEntity goal) {
        if(!(goal instanceof MoveUpLeaderboardGoalEntity))
            throw new IllegalArgumentException("Passed goal must be of type MoveUpLeaderboardGoalEntity.");

        // No additional properties to copy for this goal type
    }

    @Override
    public boolean updateProgressInternal(GoalProgressEvent goalProgressEvent, UserGoalProgressEntity userGoalProgress) {
        if(goalProgressEvent instanceof MoveLeaderboardGoalProgressEvent leaderboardGoalProgress
                && !userGoalProgress.isCompleted()) {
            userGoalProgress.setCompleted(true);
            return true;
        }

        return false;
    }

    @Override
    public boolean equalsGoalTargets(GoalEntity other) {
        return super.equalsGoalTargets(other);
    }

    @Override
    public UserGoalProgressEntity generateUserGoalProgress(UserEntity user) {
        return new UserGoalProgressEntity(user, this);
    }
}
