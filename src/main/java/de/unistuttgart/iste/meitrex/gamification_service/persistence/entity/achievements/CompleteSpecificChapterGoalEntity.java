package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "CompleteSpecificChapterGoal")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompleteSpecificChapterGoalEntity extends GoalEntity{
    @Column
    UUID chapterId;

    @Column
    String chapterName;

    public String generateDescription() {
        return "Complete the chapter " + chapterName + ".";
    }

    public void updateProgress(UserGoalProgressEntity userGoalProgress) {

    }

    public void updateProgress(UserGoalProgressEntity userGoalProgress, UUID chapterId) {
        if (chapterId.equals(this.chapterId)) {
            userGoalProgress.setCompleted(true);
        }
    }

    @Override
    public UserGoalProgressEntity generateUserGoalProgress(UserEntity user) {
        return new UserGoalProgressEntity(user, this);
    }
}
