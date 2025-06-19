package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity(name = "UserGoalProgress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserGoalProgressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column
    boolean completed;

    @ManyToOne(fetch = FetchType.LAZY)
    UserEntity user;

    @OneToOne
    GoalEntity goal;

    public void updateProgress(UserGoalProgressEntity userGoalProgress) {

    }
}
