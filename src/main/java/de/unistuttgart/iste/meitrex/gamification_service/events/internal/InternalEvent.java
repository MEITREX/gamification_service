package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

import java.time.*;
import java.util.*;

import lombok.*;

import org.springframework.context.*;


/**
 * Each {@code InternalEvent} corresponds to a {@link de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent}
 * stored in the database and is represented as a Spring {@link org.springframework.context.ApplicationEvent}. These events
 * can be published within the application context and are typically consumed by {@link AbstractInternalListener} implementations.
 * Every {@code InternalEvent} is identified by a unique {@link java.util.UUID}, which references the corresponding
 * {@link de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent} instance.
 *
 * @author Philipp Kunz
 */
@Getter
public abstract class InternalEvent extends ApplicationEvent {

    private final UUID id;

    public InternalEvent(Object source, UUID id) {
        super(source);
        this.id = Objects.requireNonNull(id);
    }

    public InternalEvent(Object source, Clock clock, UUID id) {
        super(source, clock);
        this.id = Objects.requireNonNull(id);
    }
}
