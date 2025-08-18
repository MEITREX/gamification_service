package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.CompletedSpecificContentGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.generated.dto.ContentType;
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
public class CompleteSpecificContentGoalEntity extends GoalEntity {

    private UUID contentId;
    private String contentName;
    private ContentType contentType;

    @Override
    public String generateDescription() {
        return "Successfully complete the content " + contentName + ".";
    }

    @Override
    protected void populateFromOther(GoalEntity goal) {
        if (!(goal instanceof CompleteSpecificContentGoalEntity assessmentGoal))
            throw new IllegalArgumentException("Passed goal must be of type CompleteSpecificContentGoalEntity.");

        contentId = assessmentGoal.contentId;
        contentName = assessmentGoal.contentName;
        contentType = assessmentGoal.contentType;
    }

    @Override
    public boolean updateProgress(GoalProgressEvent goalProgressEvent, UserGoalProgressEntity userGoalProgress) {
        if(goalProgressEvent instanceof CompletedSpecificContentGoalProgressEvent contentGoalProgress) {
            UUID eventContentId = contentGoalProgress.getContentId();
            if (eventContentId.equals(this.contentId)
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
                && this.contentId.equals(((CompleteSpecificContentGoalEntity) other).contentId);
    }

    @Override
    public UserGoalProgressEntity generateUserGoalProgress(UserEntity user) {
        return new UserGoalProgressEntity(user, this);
    }
}
