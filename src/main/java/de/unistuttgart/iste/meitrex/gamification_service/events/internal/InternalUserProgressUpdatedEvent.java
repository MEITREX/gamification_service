package de.unistuttgart.iste.meitrex.gamification_service.events.internal;


import lombok.Getter;

import java.time.Clock;
import java.util.UUID;

@Getter
public class InternalUserProgressUpdatedEvent extends InternalEvent {

    public InternalUserProgressUpdatedEvent(Object source, UUID id) {
        super(source, id);
    }

    public InternalUserProgressUpdatedEvent(Object source, Clock clock, UUID id) {
        super(source, clock, id);
    }
}
