package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

import java.time.Clock;
import java.util.UUID;

public class InternalMediaWorkedOnEvent extends InternalEvent {

    public InternalMediaWorkedOnEvent(Object source, UUID id) {
        super(source, id);
    }

    public InternalMediaWorkedOnEvent(Object source, Clock clock, UUID id) {
        super(source, clock, id);
    }
}
