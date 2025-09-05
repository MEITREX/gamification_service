package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.HasGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.lang.reflect.InvocationTargetException;
import java.time.*;
import java.util.Objects;
import java.util.UUID;

@Entity(name = "Goal")
@Data
@AllArgsConstructor
@SuperBuilder
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

    public GoalEntity() {
        // most ugly way to ensure that by default we have an "infinite" tracking time (I hope this system will not be
        // used anymore in 500 years). Simply using OffsetDateTime.MAX/MIN doesn't work because postgres timestamps seem
        // to have a smaller date range than Java's OffsetDateTime so we couldn't persist those values
        trackingStartTime = OffsetDateTime.now().minusYears(500);
        trackingEndTime = OffsetDateTime.now().plusYears(500);
    }

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

    /**
     * Called in order to update the given UserGoalProgressEntity of this GoalEntity when a GoalProgressEvent happens.
     * This method checks that the GoalProgressEvent is of relevance etc. and only updates the user's progress if
     * the user has truly made progress based on the GoalProgressEvent.
     * @param goalProgressEvent The goal progress event which was raised.
     * @param userGoalProgress The user's goal progress.
     * @return True if the user completed the goal, false otherwise. If the goal was already completed previously this
     * will also return false.
     */
    public final boolean updateProgress(GoalProgressEvent goalProgressEvent, UserGoalProgressEntity userGoalProgress) {
        if(trackingStartTime.isAfter(OffsetDateTime.now()) || trackingEndTime.isBefore(OffsetDateTime.now()))
            return false;

        if(!goalProgressEvent.getUserId().equals(userGoalProgress.getUser().getId()))
            return false;

        return updateProgressInternal(goalProgressEvent, userGoalProgress);
    }

    /**
     * Helper method called by the GoalEntity.updateProgress() method. Implementing classes need to implement this
     * method in order to update the passed userGoalProgress if the passed goalProgressEvent indicates that the user
     * has made progress on the goal. The method should update the passed userGoalProgress entity accordingly.
     * The method should return true if and only if the goal was completed by the user in this invocation of the method.
     * Otherwise, it should return false, including when the goal was already finished by the user previously.
     */
    protected abstract boolean updateProgressInternal(GoalProgressEvent goalProgressEvent,
                                                      UserGoalProgressEntity userGoalProgress);

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

    public void setTrackingTimeToToday() {
        ZoneId zoneId = ZoneId.systemDefault();
        ZoneOffset offset = zoneId.getRules().getOffset(Instant.now());

        setTrackingStartTime(LocalDate.now().atStartOfDay().atOffset(offset));
        setTrackingEndTime(LocalDate.now().atTime(LocalTime.MAX).atOffset(offset));
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
