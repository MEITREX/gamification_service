package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

import java.time.Clock;
import java.util.UUID;

public class InternalChapterCompletedEvent extends InternalEvent {

    public InternalChapterCompletedEvent(Object source, UUID id) {
        super(source, id);
    }

    public InternalChapterCompletedEvent(Object source, Clock clock, UUID id) {
        super(source, clock, id);
    }
}
