package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.CompletedSpecificAssessmentGoalProgressEvent;
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
public class CompleteSpecificAssessmentGoalEntity extends GoalEntity {

    private UUID assessmentId;

    private String assessmentName;

    @Override
    public String generateDescription() {
        return "Successfully complete the assessment " + assessmentName + ".";
    }

    @Override
    protected void populateFromOther(GoalEntity goal) {
        if (!(goal instanceof CompleteSpecificAssessmentGoalEntity assessmentGoal))
            throw new IllegalArgumentException("Passed goal must be of type CompleteSpecificChapterGoalEntity.");

        assessmentId = assessmentGoal.assessmentId;
        assessmentName = assessmentGoal.assessmentName;
    }

    @Override
    public boolean updateProgress(GoalProgressEvent goalProgressEvent, UserGoalProgressEntity userGoalProgress) {
        if(goalProgressEvent instanceof CompletedSpecificAssessmentGoalProgressEvent assGoalProgress) {
            UUID eventAssessmentId = assGoalProgress.getAssessmentId();
            if (eventAssessmentId.equals(this.assessmentId)
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
                && this.assessmentId.equals(((CompleteSpecificAssessmentGoalEntity) other).assessmentId);
    }

    @Override
    public UserGoalProgressEntity generateUserGoalProgress(UserEntity user) {
        return new UserGoalProgressEntity(user, this);
    }
}
