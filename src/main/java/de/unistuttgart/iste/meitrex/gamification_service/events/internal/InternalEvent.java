package de.unistuttgart.iste.meitrex.gamification_service.events.internal;


import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

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
