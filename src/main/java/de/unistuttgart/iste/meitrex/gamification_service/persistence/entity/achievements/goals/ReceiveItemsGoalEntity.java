package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.ReceiveItemsGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.CountableUserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.internal.bytebuddy.implementation.bind.annotation.Super;

import java.util.UUID;

@Entity(name = "ReceiveItemsGoal")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class ReceiveItemsGoalEntity extends CountableGoalEntity{

    @Override
    protected void populateFromOther(GoalEntity goal) {
        if (!(goal instanceof ReceiveItemsGoalEntity))
            throw new IllegalArgumentException("Passed goal must be of type ReceiveItemsGoalEntity.");
    }

    @Override
    protected boolean updateProgressInternal(GoalProgressEvent progressEvent, UserGoalProgressEntity userGoalProgressEntity) {
        if (progressEvent instanceof ReceiveItemsGoalProgressEvent &&
                userGoalProgressEntity instanceof CountableUserGoalProgressEntity countableUserGoalProgressEntity) {
            log.info("Updating progress for user goal progress");
            countableUserGoalProgressEntity.incrementCompletedCount();
            if (countableUserGoalProgressEntity.getCompletedCount() >= getRequiredCount()
                    && !countableUserGoalProgressEntity.isCompleted()) {
                countableUserGoalProgressEntity.setCompleted(true);
                return true;
            }
        }
        return false;
    }

    @Override
    public String generateDescription() {
        return "Receive " + super.getRequiredCount() + " Items.";
    }
}
