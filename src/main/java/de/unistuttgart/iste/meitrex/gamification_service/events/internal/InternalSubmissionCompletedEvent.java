package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

import java.time.Clock;
import java.util.UUID;

public class InternalSubmissionCompletedEvent extends InternalEvent{

    public InternalSubmissionCompletedEvent(Object source, UUID id) {
        super(source, id);
    }

    public InternalSubmissionCompletedEvent(Object source, Clock clock, UUID id) {
        super(source, clock, id);
    }
}
