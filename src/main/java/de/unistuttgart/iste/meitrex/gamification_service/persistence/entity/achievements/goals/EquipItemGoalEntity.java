package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.EquipItemGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity(name = "EquipItemGoal")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EquipItemGoalEntity extends GoalEntity {

    @Override
    public String generateDescription() {
        return "Euqip an item.";
    }

    @Override
    protected void populateFromOther(GoalEntity goal) {
        if (!(goal instanceof EquipItemGoalEntity chapterGoal))
            throw new IllegalArgumentException("Passed goal must be of type EquipItemGoalEntity.");
    }

    @Override
    public boolean updateProgressInternal(GoalProgressEvent progressEvent, UserGoalProgressEntity userGoalProgress) {
        if (progressEvent instanceof EquipItemGoalProgressEvent && !userGoalProgress.isCompleted()) {
            userGoalProgress.setCompleted(true);
            return true;
        }
        return false;
    }

    @Override
    public UserGoalProgressEntity generateUserGoalProgress(UserEntity user) {
        return new UserGoalProgressEntity(user, this);
    }
}
