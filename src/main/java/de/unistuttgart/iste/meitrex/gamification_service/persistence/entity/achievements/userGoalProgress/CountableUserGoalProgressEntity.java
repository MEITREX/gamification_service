package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.LoginStreakGoalEntity;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;


@Entity(name = "CountableUserGoalProgress")
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class CountableUserGoalProgressEntity extends UserGoalProgressEntity{
    @Column
    int completedCount;

    @ElementCollection
    List<UUID> contentIds;

    @ElementCollection
    List<OffsetDateTime> loginTimes;

    public CountableUserGoalProgressEntity(UserEntity user, @NotNull GoalEntity goal) {
        super(user, goal);
        contentIds = new ArrayList<>();
        loginTimes = new ArrayList<>();
        completedCount = 0;
    }

    public void updateProgress(OffsetDateTime loginTime) {
        if (super.getGoal() instanceof LoginStreakGoalEntity) {
            if (loginTimes.isEmpty()) {
                completedCount = 1;
                loginTimes.add(loginTime);
            }else if (getDifferenceInDays(loginTimes.getLast(), loginTime) > 1) {
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
        return ChronoUnit.DAYS.between(firstTime.toLocalDate(), secondTime.toLocalDate());
    }


    @Override
    public String toString() {
        return "CountableUserGoalProgressEntity{" +
                " super=" + super.toString() +
                ", completedCount=" + completedCount +
                ", contentIds=" + contentIds +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CountableUserGoalProgressEntity that = (CountableUserGoalProgressEntity) o;
        return completedCount == that.completedCount && Objects.equals(contentIds, that.contentIds) && Objects.equals(loginTimes, that.loginTimes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), completedCount, contentIds, loginTimes);
    }
}
