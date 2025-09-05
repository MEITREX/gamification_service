package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

import java.time.Clock;
import java.util.UUID;

public class InternalAskedTutorAQuestionEvent extends InternalEvent {
    public InternalAskedTutorAQuestionEvent(Object source, UUID id) {
        super(source, id);
    }

    public InternalAskedTutorAQuestionEvent(Object source, Clock clock, UUID id) {
        super(source, clock, id);
    }
}
