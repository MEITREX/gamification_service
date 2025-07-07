package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CombineUserGoalProgressEntity extends UserGoalProgressEntity {
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    UserGoalProgressEntity userGoalProgressEntity1;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    UserGoalProgressEntity userGoalProgressEntity2;

    public CombineUserGoalProgressEntity(UserEntity user, GoalEntity goal, UserGoalProgressEntity userGoalProgressEntity1, UserGoalProgressEntity userGoalProgressEntity2) {
        setUser(user);
        setGoal(goal);
        this.userGoalProgressEntity1 = userGoalProgressEntity1;
        this.userGoalProgressEntity2 = userGoalProgressEntity2;
    }
}
