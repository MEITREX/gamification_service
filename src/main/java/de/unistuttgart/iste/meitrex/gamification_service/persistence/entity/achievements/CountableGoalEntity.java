package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;


@Entity(name = "CountableGoal")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public abstract class CountableGoalEntity extends GoalEntity{
    @Column
    int requiredCount;

    public abstract String generateDescription();

    public abstract void updateProgress(UserGoalProgressEntity userGoalProgress);

    @Override
    public UserGoalProgressEntity generateUserGoalProgress(UserEntity user) {
        return new CountableUserGoalProgressEntity(user, this);
    }

    @Override
    public String toString() {
        return "CountableGoalEntity{" +
                "super=" + super.toString() +
                ", requiredCount=" + requiredCount +
                '}';
    }
}
