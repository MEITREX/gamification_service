package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.CompletedSpecificChapterGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity(name = "CompleteSpecificChapterGoal")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompleteSpecificChapterGoalEntity extends GoalEntity {
    @Column
    UUID chapterId;

    @Column
    String chapterName;

    @Override
    public String generateDescription() {
        return "Complete the chapter " + chapterName + ".";
    }

    @Override
    protected void populateFromOther(GoalEntity goal) {
        if (!(goal instanceof CompleteSpecificChapterGoalEntity chapterGoal))
            throw new IllegalArgumentException("Passed goal must be of type CompleteSpecificChapterGoalEntity.");

        chapterId = chapterGoal.getChapterId();
        chapterName = chapterGoal.getChapterName();
    }

    @Override
    public boolean updateProgressInternal(GoalProgressEvent progressEvent, UserGoalProgressEntity userGoalProgress) {
        if (progressEvent instanceof CompletedSpecificChapterGoalProgressEvent completedSpecificChapterGoalProgressEvent) {
            UUID eventChapterId = completedSpecificChapterGoalProgressEvent.getChapterId();
            if (eventChapterId.equals(this.chapterId)
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
                && this.chapterId.equals(((CompleteSpecificChapterGoalEntity)other).chapterId);
    }

    @Override
    public UserGoalProgressEntity generateUserGoalProgress(UserEntity user) {
        return new UserGoalProgressEntity(user, this);
    }
}
