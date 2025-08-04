package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity(name = "CombineUserGoalProgress")
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
        super(user, goal);
        this.userGoalProgressEntity1 = userGoalProgressEntity1;
        this.userGoalProgressEntity2 = userGoalProgressEntity2;
    }
}
