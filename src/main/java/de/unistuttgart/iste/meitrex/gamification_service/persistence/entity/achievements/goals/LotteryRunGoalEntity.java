package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;


import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.LotteryRunGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.CountableUserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Entity(name = "LotteryRunGoal")
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class LotteryRunGoalEntity extends CountableGoalEntity {


    @Override
    public String generateDescription() {
        return "Open " + super.getRequiredCount() + " eggs in the lottery.";
    }

    @Override
    protected void populateFromOther(GoalEntity goal) {
        if (!(goal instanceof LotteryRunGoalEntity quizzesGoal))
            throw new IllegalArgumentException("Passed goal must be of type LotteryRunGoalEntity.");
    }

    @Override
    public boolean updateProgressInternal(GoalProgressEvent progressEvent, UserGoalProgressEntity userGoalProgressEntity) {
        if (progressEvent instanceof LotteryRunGoalProgressEvent &&
                userGoalProgressEntity instanceof CountableUserGoalProgressEntity countableUserGoalProgressEntity) {
            countableUserGoalProgressEntity.incrementCompletedCount();
            if (countableUserGoalProgressEntity.getCompletedCount() >= getRequiredCount()
                    && !countableUserGoalProgressEntity.isCompleted()) {
                countableUserGoalProgressEntity.setCompleted(true);
                return true;
            }
        }

        return false;
    }
}
