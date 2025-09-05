package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Value
@SuperBuilder
public class CompletedSpecificContentGoalProgressEvent extends GoalProgressEvent {
    UUID contentId;

    public CompletedSpecificContentGoalProgressEvent(UUID userId, UUID contentId) {
        super(userId);
        this.contentId = contentId;
    }
}
