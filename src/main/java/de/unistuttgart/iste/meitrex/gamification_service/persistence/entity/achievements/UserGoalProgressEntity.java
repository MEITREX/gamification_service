package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Entity(name = "UserGoalProgress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserGoalProgressEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column
    boolean completed;

    @ManyToOne(fetch = FetchType.LAZY)
    UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    GoalEntity goal;

    public UserGoalProgressEntity(UserEntity user, GoalEntity goal) {
        this.user = user;
        this.goal = goal;
        completed = false;
    }

    public void updateProgress() {

    }

    @Override
    public String toString() {
        return "UserGoalProgressEntity{" +
                "id=" + id +
                ", completed=" + completed +
                ", user=" + user.getId() +
                ", goal=" + goal.getId() +
                '}';
    }
}
