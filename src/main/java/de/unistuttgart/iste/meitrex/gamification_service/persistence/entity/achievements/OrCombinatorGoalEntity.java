package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity(name = "OrCombinatorGoal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrCombinatorGoalEntity extends GoalEntity{

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    GoalEntity goal1;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    GoalEntity goal2;

    public String generateDescription() {
        return "";
    }

    public void updateProgress(UserGoalProgressEntity userGoalProgress) {

    }

    //@Override
    @Override
    public UserGoalProgressEntity generateUserGoalProgress(UserEntity user, GoalEntity goal) {
        if (goal instanceof OrCombinatorGoalEntity orCombinatorGoalEntity) {
            return new CombineUserGoalProgressEntity(user, goal,
                    goal1.generateUserGoalProgress(user, orCombinatorGoalEntity.getGoal1()),
                    goal2.generateUserGoalProgress(user, orCombinatorGoalEntity.getGoal2()));
        }
        return null;
    }
}
