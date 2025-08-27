package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;

@EqualsAndHashCode(callSuper = true)
@Value
@SuperBuilder
public class LoginStreakGoalProgressEvent extends GoalProgressEvent{
    OffsetDateTime loginTime;
}
