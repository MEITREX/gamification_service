package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Value
@SuperBuilder
public class CompletedQuizzesGoalProgressEvent extends GoalProgressEvent {
    float score;
    UUID contentId;

    public CompletedQuizzesGoalProgressEvent(UUID userId, float score, UUID contentId) {
        super(userId);
        this.score = score;
        this.contentId = contentId;
    }
}
