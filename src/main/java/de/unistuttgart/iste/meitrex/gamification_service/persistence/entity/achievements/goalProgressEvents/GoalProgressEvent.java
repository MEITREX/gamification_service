package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@SuperBuilder
public abstract class GoalProgressEvent {
    final UUID userId;
}
