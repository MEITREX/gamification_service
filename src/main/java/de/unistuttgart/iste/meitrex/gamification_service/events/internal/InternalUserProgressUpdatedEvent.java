package de.unistuttgart.iste.meitrex.gamification_service.events.internal;


import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserProgressUpdatedEvent;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.*;
import java.util.*;

@Getter
public class InternalUserProgressUpdatedEvent extends InternalEvent {

    public InternalUserProgressUpdatedEvent(Object source, UUID id) {
        super(source, id);
    }

    public InternalUserProgressUpdatedEvent(Object source, Clock clock, UUID id) {
        super(source, clock, id);
    }
}
