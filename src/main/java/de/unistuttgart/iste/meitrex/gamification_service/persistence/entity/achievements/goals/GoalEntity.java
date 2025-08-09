package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.HasGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.lang.reflect.InvocationTargetException;
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

    @OneToOne(cascade = CascadeType.ALL)
    HasGoalEntity parentWithGoal;

    /**
     * Creates a new instance of the GoalEntity, copying all properties.
     * @return A new instance of the GoalEntity with the same properties as this one.
     */
    public GoalEntity clone() {
        GoalEntity newGoal = null;
        try {
            newGoal = this.getClass().getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException |
                 InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        newGoal.setTrackingStartTime(trackingStartTime);
        newGoal.setTrackingEndTime(trackingEndTime);

        populateFromOther(newGoal);
        return newGoal;
    }

    /**
     * Populates the properties of this GoalEntity from another GoalEntity.
     * @param goal The GoalEntity to copy properties from.
     */
    protected abstract void populateFromOther(GoalEntity goal);

    public abstract boolean updateProgress(GoalProgressEvent goalProgressEvent, UserGoalProgressEntity userGoalProgress);

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

    /**
     * Used to compare 2 goal entities in regard to their actual goal targets, ignoring ID, parent entity, etc.
     * @param other The entity to compare to
     * @return True if the entities are equal, false otherwise.
     */
    public boolean equalsGoalTargets(GoalEntity other) {
        if(!getClass().equals(other.getClass()))
            return false;

        return Objects.equals(trackingStartTime, other.trackingStartTime)
                && Objects.equals(trackingEndTime, other.trackingEndTime);
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
