package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

import java.time.Clock;
import java.util.UUID;

public class InternalMediaRecordInfoEvent extends InternalEvent {

    public InternalMediaRecordInfoEvent(Object source, UUID id) {
        super(source, id);
    }

    public InternalMediaRecordInfoEvent(Object source, Clock clock, UUID id) {
        super(source, clock, id);
    }
}
