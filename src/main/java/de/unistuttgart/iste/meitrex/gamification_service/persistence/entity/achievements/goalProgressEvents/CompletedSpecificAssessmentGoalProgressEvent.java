package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Value
@SuperBuilder
public class CompletedSpecificAssessmentGoalProgressEvent extends GoalProgressEvent {
    UUID assessmentId;

    public CompletedSpecificAssessmentGoalProgressEvent(UUID userId, UUID assessmentId) {
        super(userId);
        this.assessmentId = assessmentId;
    }
}
