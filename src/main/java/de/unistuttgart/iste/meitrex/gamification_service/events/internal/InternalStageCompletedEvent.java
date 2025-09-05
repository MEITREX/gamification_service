package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

import java.time.Clock;
import java.util.UUID;

public class InternalStageCompletedEvent extends InternalEvent {

    public InternalStageCompletedEvent(Object source, UUID id) {
        super(source, id);
    }

    public InternalStageCompletedEvent(Object source, Clock clock, UUID id) {
        super(source, clock, id);
    }
}
