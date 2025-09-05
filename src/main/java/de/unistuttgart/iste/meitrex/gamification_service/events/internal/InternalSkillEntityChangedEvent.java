package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

import java.time.Clock;
import java.util.UUID;

public class InternalSkillEntityChangedEvent extends InternalEvent {


    public InternalSkillEntityChangedEvent(Object source, UUID id) {
        super(source, id);
    }

    public InternalSkillEntityChangedEvent(Object source, Clock clock, UUID id) {
        super(source, clock, id);
    }

}
