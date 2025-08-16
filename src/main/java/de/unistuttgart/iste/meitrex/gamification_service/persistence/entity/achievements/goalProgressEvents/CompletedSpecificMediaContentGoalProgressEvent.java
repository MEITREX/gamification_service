package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Value
@SuperBuilder
public class CompletedSpecificMediaContentGoalProgressEvent extends GoalProgressEvent {
    UUID mediaContentId;

    public CompletedSpecificMediaContentGoalProgressEvent(UUID userId, UUID mediaContentId) {
        super(userId);
        this.mediaContentId = mediaContentId;
    }
}
