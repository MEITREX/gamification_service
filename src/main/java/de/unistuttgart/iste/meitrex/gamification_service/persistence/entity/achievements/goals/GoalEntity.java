package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import jakarta.persistence.*;
import lombok.*;
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
    AchievementEntity achievement;

    public abstract void updateProgress(GoalProgressEvent goalProgressEvent, UserGoalProgressEntity userGoalProgress);

    public abstract UserGoalProgressEntity generateUserGoalProgress(UserEntity user);

    public abstract String generateDescription();

    @Override
    public String toString() {
        if (achievement == null || achievement.getId() == null) {
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
                ", achievement=" + achievement.getId().toString() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoalEntity that = (GoalEntity) o;
        if (achievement == null && that.achievement == null) {
            return Objects.equals(id, that.id) && Objects.equals(trackingStartTime, that.trackingStartTime) && Objects.equals(trackingEndTime, that.trackingEndTime);
        }
        if ((achievement == null) || (that.achievement == null)) return false;
        return Objects.equals(id, that.id) && Objects.equals(trackingStartTime, that.trackingStartTime) && Objects.equals(trackingEndTime, that.trackingEndTime) && Objects.equals(achievement.getId(), that.achievement.getId());
    }

    @Override
    public int hashCode() {
        if (achievement == null) return Objects.hash(id, trackingStartTime, trackingEndTime, null);
        return Objects.hash(id, trackingStartTime, trackingEndTime, achievement.getId());
    }
}
