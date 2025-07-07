package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.AchievementRepository;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
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

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    AchievementEntity achievement;

    public abstract void updateProgress(UserGoalProgressEntity userGoalProgress);

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
        if (achievement == null && that.achievement == null) return true;
        if ((achievement == null) || (that.achievement == null)) return false;
        return Objects.equals(id, that.id) && Objects.equals(trackingStartTime, that.trackingStartTime) && Objects.equals(trackingEndTime, that.trackingEndTime) && Objects.equals(achievement.getId(), that.achievement.getId());
    }

    @Override
    public int hashCode() {
        if (achievement == null) return Objects.hash(id, trackingStartTime, trackingEndTime, null);
        return Objects.hash(id, trackingStartTime, trackingEndTime, achievement.getId());
    }
}
