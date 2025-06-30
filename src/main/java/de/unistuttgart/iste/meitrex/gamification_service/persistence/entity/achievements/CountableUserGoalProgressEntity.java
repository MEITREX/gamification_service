package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import com.google.type.DateTime;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
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
    @Column
    int completedCount;

    @ElementCollection
    List<UUID> contentIds;

    @ElementCollection
    List<OffsetDateTime> loginTimes;

    public CountableUserGoalProgressEntity(UserEntity user, @NotNull CountableGoalEntity goal) {
        super(user, goal);
        contentIds = new ArrayList<>();
        completedCount = 0;
    }

    public void updateProgress() {
        CountableGoalEntity goal = (CountableGoalEntity) super.getGoal();
        goal.updateProgress(this);
    }

    public void updateProgress(OffsetDateTime loginTime) {
        if (super.getGoal() instanceof LoginStreakGoalEntity) {
            if (getDifferenceInDays(loginTimes.getLast(), loginTime) > 1) {
                completedCount = 0;
            } else if (getDifferenceInDays(loginTimes.getLast(), loginTime) == 1) {
                completedCount++;
                if (completedCount >= ((LoginStreakGoalEntity) super.getGoal()).getRequiredCount()) {
                    setCompleted(true);
                }
            }
            loginTimes.add(loginTime);
        }
    }

    private long getDifferenceInDays(OffsetDateTime firstTime, OffsetDateTime secondTime) {
        return ChronoUnit.DAYS.between(firstTime, secondTime);
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
                " super=" + super.toString() +
                ", completedCount=" + completedCount +
                ", contentIds=" + contentIds +
                '}';
    }
}
