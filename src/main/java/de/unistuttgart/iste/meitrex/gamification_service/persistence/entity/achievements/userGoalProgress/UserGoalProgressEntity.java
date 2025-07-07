package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.UUID;

@Entity(name = "UserGoalProgress")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserGoalProgressEntity implements IWithId<UUID> {
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

    public void updateProgress(GoalProgressEvent progressEvent) {
        goal.updateProgress(progressEvent, this);
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserGoalProgressEntity that = (UserGoalProgressEntity) o;
        if (user == null && that.user == null) {
            return completed == that.completed && Objects.equals(id, that.id) && Objects.equals(goal, that.goal);
        }
        if (user == null || that.user == null) {
            return false;
        }
        return completed == that.completed && Objects.equals(id, that.id) && Objects.equals(user.getId(), that.user.getId()) && Objects.equals(goal, that.goal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, completed, user, goal);
    }
}
