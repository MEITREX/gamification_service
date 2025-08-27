package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

import java.time.Clock;
import java.util.UUID;

public class InternalContentProgressedEvent extends InternalEvent {


    public InternalContentProgressedEvent(Object source, UUID id) {
        super(source, id);
    }

    public InternalContentProgressedEvent(Object source, Clock clock, UUID id) {
        super(source, clock, id);
    }

}
