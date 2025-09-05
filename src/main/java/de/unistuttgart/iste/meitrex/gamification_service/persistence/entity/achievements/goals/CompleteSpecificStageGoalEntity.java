package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.CompletedSpecificStageGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity(name = "CompleteSpecificStageGoal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CompleteSpecificStageGoalEntity extends GoalEntity {

    private UUID stageId;
    /**
     * Zero-indexed position of the stage in the section.
     */
    private int stagePosition;
    private String sectionName;

    @Override
    public String generateDescription() {
        return "Complete the stage " + (stagePosition + 1) + " in section " + sectionName + ".";
    }

    @Override
    protected void populateFromOther(GoalEntity goal) {
        if(!(goal instanceof CompleteSpecificStageGoalEntity stageGoal))
            throw new IllegalArgumentException("Passed goal must be of type CompleteSpecificStageGoalEntity.");

        stageId = stageGoal.stageId;
        stagePosition = stageGoal.stagePosition;
        sectionName = stageGoal.sectionName;
    }

    @Override
    protected boolean updateProgressInternal(GoalProgressEvent goalProgressEvent, UserGoalProgressEntity userGoalProgress) {
        if(goalProgressEvent instanceof CompletedSpecificStageGoalProgressEvent stageGoalProgress) {
            UUID eventStageId = stageGoalProgress.getStageId();
            if(eventStageId.equals(this.stageId)
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
                && this.stageId.equals(((CompleteSpecificStageGoalEntity) other).stageId);
    }

    @Override
    public UserGoalProgressEntity generateUserGoalProgress(UserEntity user) {
        return new UserGoalProgressEntity(user, this);
    }

}
