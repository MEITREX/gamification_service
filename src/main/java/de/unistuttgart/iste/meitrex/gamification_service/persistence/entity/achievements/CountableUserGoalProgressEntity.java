package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Entity(name = "CountableUserGoalProgress")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CountableUserGoalProgressEntity extends UserGoalProgressEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column
    int completedCount;

    @ElementCollection
    List<UUID> contentIds;

    public CountableUserGoalProgressEntity(UserEntity user, @NotNull CountableGoalEntity goal) {
        super(user, goal);
        contentIds = new ArrayList<>();
        completedCount = 0;
    }

    public void updateProgress() {
        CountableGoalEntity goal = (CountableGoalEntity) super.getGoal();
        goal.updateProgress(this);
    }

    public void updateProgress(float score, UUID contentId) {
        if(contentIds == null) {
            contentIds = new ArrayList<>();
        }
        if (super.getGoal() instanceof CompletedQuizzesGoalEntity completedQuizzesGoalEntity) {
            completedQuizzesGoalEntity.updateProgress(this, score, contentId);
        } else {
            super.getGoal().updateProgress(this);
        }
    }

    @Override
    public String toString() {
        return "CountableUserGoalProgressEntity{" +
                "id=" + id +
                ", completedCount=" + completedCount +
                ", contentIds=" + contentIds +
                ", super=" + super.toString() +
                '}';
    }
}
