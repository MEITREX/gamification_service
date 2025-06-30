package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity(name = "AndCombinatorGoal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AndCombinatorGoalEntity extends GoalEntity{
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    GoalEntity goal1;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    GoalEntity goal2;

    public String generateDescription() {
        return "";
    }

    public void updateProgress(UserGoalProgressEntity userGoalProgress) {

    }


    @Override
    public UserGoalProgressEntity generateUserGoalProgress(UserEntity user, GoalEntity goal) {
        if (goal instanceof AndCombinatorGoalEntity andCombinatorGoalEntity) {
            return new CombineUserGoalProgressEntity(user, goal,
                    goal1.generateUserGoalProgress(user, andCombinatorGoalEntity.getGoal1()),
                    goal2.generateUserGoalProgress(user, andCombinatorGoalEntity.getGoal2()));
        }
        return null;
    }
}
