package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "CompleteSpecificChapterGoal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CompleteSpecificChapterGoalEntity extends GoalEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column
    UUID chapterId;

    public String generateDescription() {
        return "";
    }

    public void updateProgress(UserGoalProgressEntity userGoalProgress) {

    }


    @Override
    public List<UserGoalProgressEntity> generateUserGoalProgress() {
        UserGoalProgressEntity userGoalProgress = new UserGoalProgressEntity();
        userGoalProgress.setGoal(this);
        userGoalProgress.setCompleted(false);
        List<UserGoalProgressEntity> userGoalProgressEntities = new ArrayList<>();
        userGoalProgressEntities.add(userGoalProgress);
        return userGoalProgressEntities;
    }
}
