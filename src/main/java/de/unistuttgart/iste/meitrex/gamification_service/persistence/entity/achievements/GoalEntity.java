package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.AchievementRepository;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity(name = "Goal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class GoalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column
    OffsetDateTime trackingStartTime;

    @Column
    OffsetDateTime trackingEndTime;

    @OneToOne
    AchievementEntity achievement;

    @OneToMany
    List<UserGoalProgressEntity> userGoalProgress;

    public abstract List<UserGoalProgressEntity> generateUserGoalProgress();
}
