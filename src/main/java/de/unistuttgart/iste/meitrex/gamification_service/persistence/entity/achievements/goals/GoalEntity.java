package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.HasGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "Goal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class GoalEntity implements IWithId<UUID> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column
    OffsetDateTime trackingStartTime;

    @Column
    OffsetDateTime trackingEndTime;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    HasGoalEntity parentWithGoal;

    public abstract void updateProgress(GoalProgressEvent goalProgressEvent, UserGoalProgressEntity userGoalProgress);

    public abstract UserGoalProgressEntity generateUserGoalProgress(UserEntity user);

    public abstract String generateDescription();

    @Override
    public String toString() {
        if (parentWithGoal == null || parentWithGoal.getId() == null) {
            return "GoalEntity{" +
                    "id=" + id +
                    ", trackingStartTime=" + trackingStartTime +
                    ", trackingEndTime=" + trackingEndTime +
                    ", achievement=null" +
                    '}';
        }
        return "GoalEntity{" +
                "id=" + id +
                ", trackingStartTime=" + trackingStartTime +
                ", trackingEndTime=" + trackingEndTime +
                ", achievement=" + parentWithGoal.getId().toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoalEntity that = (GoalEntity) o;
        if (parentWithGoal == null && that.parentWithGoal == null) {
            return Objects.equals(id, that.id) && Objects.equals(trackingStartTime, that.trackingStartTime) && Objects.equals(trackingEndTime, that.trackingEndTime);
        }
        if ((parentWithGoal == null) || (that.parentWithGoal == null)) return false;
        return Objects.equals(id, that.id) && Objects.equals(trackingStartTime, that.trackingStartTime) && Objects.equals(trackingEndTime, that.trackingEndTime) && Objects.equals(parentWithGoal.getId(), that.parentWithGoal.getId());
    }

    @Override
    public int hashCode() {
        if (parentWithGoal == null) return Objects.hash(id, trackingStartTime, trackingEndTime, null);
        return Objects.hash(id, trackingStartTime, trackingEndTime, parentWithGoal.getId());
    }
}
