package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents;

import lombok.EqualsAndHashCode;
import lombok.Value;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Value
@SuperBuilder
public class AnswerForumGoalProgressEvent extends GoalProgressEvent{
    public AnswerForumGoalProgressEvent(UUID userId) {
        super(userId);
    }
}
