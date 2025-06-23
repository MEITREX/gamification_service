package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity(name = "CountableGoal")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public abstract class CountableGoalEntity extends GoalEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column
    int requiredCount;

    public abstract String generateDescription();

    public abstract void updateProgress(UserGoalProgressEntity userGoalProgress);

    @Override
    public UserGoalProgressEntity generateUserGoalProgress(UserEntity user, GoalEntity goalEntity) {
        return new CountableUserGoalProgressEntity(user, (CountableGoalEntity) goalEntity);
    }

    @Override
    public String toString() {
        return "CountableGoalEntity{" +
                "id=" + id +
                ", requiredCount=" + requiredCount +
                ", super=" + super.toString() +
                '}';
    }
}
