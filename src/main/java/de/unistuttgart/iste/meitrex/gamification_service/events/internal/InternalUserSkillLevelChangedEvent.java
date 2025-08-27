package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

import java.time.Clock;
import java.util.UUID;

public class InternalUserSkillLevelChangedEvent extends InternalEvent {


    public InternalUserSkillLevelChangedEvent(Object source, UUID id) {
        super(source, id);
    }

    public InternalUserSkillLevelChangedEvent(Object source, Clock clock, UUID id) {
        super(source, clock, id);
    }

}
