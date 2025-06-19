package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity(name = "CountableUserGoalProgress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CountableUserGoalProgressEntity extends UserGoalProgressEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column
    int completedCount;

    @OneToOne
    @NotNull
    CountableGoalEntity goal;

    @ElementCollection
    List<UUID> contentIds;

    public void updateProgress() {
        goal.updateProgress(this);
    }

    public void updateProgress(float score, UUID contentId) {
        if(contentIds == null) {
            contentIds = new ArrayList<>();
        }
        if (goal instanceof CompletedQuizzesGoalEntity completedQuizzesGoalEntity) {
            completedQuizzesGoalEntity.updateProgress(this, score, contentId);
        } else {
            goal.updateProgress(this);
        }
    }
}
