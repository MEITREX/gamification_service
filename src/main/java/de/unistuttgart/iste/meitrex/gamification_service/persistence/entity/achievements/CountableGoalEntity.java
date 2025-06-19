package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "CountableGoal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class CountableGoalEntity extends GoalEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column
    int requiredCount;

    public abstract String generateDescription();

    public abstract void updateProgress(UserGoalProgressEntity userGoalProgress);

    @Override
    public List<UserGoalProgressEntity> generateUserGoalProgress() {
        CountableUserGoalProgressEntity userGoalProgress = new CountableUserGoalProgressEntity();
        userGoalProgress.setGoal(this);
        userGoalProgress.setCompleted(false);
        userGoalProgress.setContentIds(new ArrayList<>());
        userGoalProgress.setCompletedCount(0);
        List<UserGoalProgressEntity> userGoalProgressEntities = new ArrayList<>();
        userGoalProgressEntities.add(userGoalProgress);
        return userGoalProgressEntities;
    }
}
