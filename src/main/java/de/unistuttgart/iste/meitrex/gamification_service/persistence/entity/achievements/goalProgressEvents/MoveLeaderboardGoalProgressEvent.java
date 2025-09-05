package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Value
@SuperBuilder
public class MoveLeaderboardGoalProgressEvent extends GoalProgressEvent {
    int oldRank;
    int newRank;
}
