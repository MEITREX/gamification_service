package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents;

import de.unistuttgart.iste.meitrex.common.event.TutorCategory;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Value
@SuperBuilder
public class AskedTutorAQuestionGoalProgressEvent extends GoalProgressEvent {
}
