package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

import java.time.Clock;
import java.util.UUID;

public class InternalForumActivityEvent extends InternalEvent{

    public InternalForumActivityEvent(Object source, UUID id) {
        super(source, id);
    }

    public InternalForumActivityEvent(Object source, Clock clock, UUID id) {
        super(source, clock, id);
    }
}
