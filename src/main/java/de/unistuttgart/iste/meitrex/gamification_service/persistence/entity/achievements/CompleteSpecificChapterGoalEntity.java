package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "CompleteSpecificChapterGoal")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
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
    public UserGoalProgressEntity generateUserGoalProgress(UserEntity user, GoalEntity goalEntity) {
        return new UserGoalProgressEntity(user, goalEntity);
    }
}
