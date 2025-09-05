package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

import java.time.Clock;
import java.util.UUID;

public class InternalCourseCompletedEvent extends InternalEvent {

    public InternalCourseCompletedEvent(Object source, UUID id) {
        super(source, id);
    }

    public InternalCourseCompletedEvent(Object source, Clock clock, UUID id) {
        super(source, clock, id);
    }

}
