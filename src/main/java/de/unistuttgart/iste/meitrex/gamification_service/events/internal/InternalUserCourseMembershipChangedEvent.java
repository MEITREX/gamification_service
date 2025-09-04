package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

import java.time.Clock;
import java.util.UUID;

public class InternalUserCourseMembershipChangedEvent extends InternalEvent {
    public InternalUserCourseMembershipChangedEvent(Object source, UUID id) {
        super(source, id);
    }

    public InternalUserCourseMembershipChangedEvent(Object source, Clock clock, UUID id) {
        super(source, clock, id);
    }
}
