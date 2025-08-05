package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.CompletedQuizzesGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.CountableUserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Entity(name = "CompletedQuizzesGoal")
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class CompletedQuizzesGoalEntity extends CountableGoalEntity implements IWithId<UUID> {
    @Column
    float minimumScore;

    @Override
    public String generateDescription() {
        return "Complete " + super.getRequiredCount() + " quizzes.";
    }

    @Override
    protected void populateFromOther(GoalEntity goal) {
        if (!(goal instanceof CompletedQuizzesGoalEntity quizzesGoal))
            throw new IllegalArgumentException("Passed goal must be of type CompletedQuizzesGoalEntity.");

        minimumScore = quizzesGoal.getMinimumScore();
    }

    @Override
    public boolean updateProgress(GoalProgressEvent progressEvent, UserGoalProgressEntity userGoalProgressEntity) {
        if (progressEvent instanceof CompletedQuizzesGoalProgressEvent completedQuizzesGoalProgressEvent &&
                userGoalProgressEntity instanceof CountableUserGoalProgressEntity countableUserGoalProgressEntity) {
            float score = completedQuizzesGoalProgressEvent.getScore();
            UUID contentId = completedQuizzesGoalProgressEvent.getContentId();
            log.info("Updating progress for user goal progress with minimum Score {} with score {} and contentId {}",
                    minimumScore, score, contentId);
            if (score >= minimumScore /*&& !countableUserGoalProgressEntity.getContentIds().contains(contentId)*/) {
                countableUserGoalProgressEntity.setCompletedCount(countableUserGoalProgressEntity.getCompletedCount() + 1);
                countableUserGoalProgressEntity.getContentIds().add(contentId);
            }
            if (countableUserGoalProgressEntity.getCompletedCount() >= getRequiredCount()
                    && !countableUserGoalProgressEntity.isCompleted()) {
                countableUserGoalProgressEntity.setCompleted(true);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean equalsGoalTargets(GoalEntity other) {
        return super.equalsGoalTargets(other)
                && minimumScore == ((CompletedQuizzesGoalEntity)other).minimumScore;
    }

    @Override
    public String toString() {
        return "CompletedQuizzesGoalEntity{" +
                "super=" + super.toString() +
                ", minimumScore=" + minimumScore +
                '}';
    }
}
