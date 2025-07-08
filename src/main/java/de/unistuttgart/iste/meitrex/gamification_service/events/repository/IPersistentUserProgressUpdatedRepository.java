package de.unistuttgart.iste.meitrex.gamification_service.events.repository;

import org.springframework.data.jpa.repository.*;

import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.*;

import java.util.*;


public interface IPersistentUserProgressUpdatedRepository extends JpaRepository<PersistentUserProgressUpdatedEvent, UUID> {
    List<PersistentUserProgressUpdatedEvent> findByStatus(PersistentEvent.Status status);
}
