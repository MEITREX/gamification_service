package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.CompletedSpecificMediaContentGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CompleteSpecificMediaContentGoalEntity extends GoalEntity {
    private UUID mediaContentId;
    private String mediaContentName;

    @Override
    public String generateDescription() {
        return "Work on the lecture materials for \"" + mediaContentName + "\".";
    }

    @Override
    protected void populateFromOther(GoalEntity goal) {
        if(!(goal instanceof CompleteSpecificMediaContentGoalEntity mediaGoal))
            throw new IllegalArgumentException("Passed goal must be of type CompleteSpecificMediaContentGoalEntity.");

        mediaContentId = mediaGoal.mediaContentId;
        mediaContentName = mediaGoal.mediaContentName;
    }

    @Override
    public boolean updateProgress(GoalProgressEvent goalProgressEvent, UserGoalProgressEntity userGoalProgress) {
        if(goalProgressEvent instanceof CompletedSpecificMediaContentGoalProgressEvent mediaGoalProgressEvent) {
            UUID mediaContentId = mediaGoalProgressEvent.getMediaContentId();
            if(mediaContentId.equals(this.mediaContentId)
                    && !userGoalProgress.isCompleted()) {
                userGoalProgress.setCompleted(true);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equalsGoalTargets(GoalEntity other) {
        return super.equalsGoalTargets(other)
                && this.mediaContentId.equals(((CompleteSpecificMediaContentGoalEntity) other).mediaContentId);
    }

    @Override
    public UserGoalProgressEntity generateUserGoalProgress(UserEntity user) {
        return new UserGoalProgressEntity(user, this);
    }
}
