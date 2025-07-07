package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.CountableUserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
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
