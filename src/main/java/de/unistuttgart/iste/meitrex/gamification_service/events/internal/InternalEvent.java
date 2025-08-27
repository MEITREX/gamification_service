package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

import java.time.*;
import java.util.*;

import lombok.*;

import org.springframework.context.*;


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
