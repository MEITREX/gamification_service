package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "AndCombinatorGoal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AndCombinatorGoalEntity extends GoalEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @OneToOne
    GoalEntity goal1;

    @OneToOne
    GoalEntity goal2;

    public String generateDescription() {
        return "";
    }

    public void updateProgress(UserGoalProgressEntity userGoalProgress) {

    }

    @Override
    public List<UserGoalProgressEntity> generateUserGoalProgress() {
        List<UserGoalProgressEntity> userGoalProgress = new ArrayList<>(goal1.generateUserGoalProgress());
        userGoalProgress.addAll(goal2.generateUserGoalProgress());
        return userGoalProgress;
    }
}
