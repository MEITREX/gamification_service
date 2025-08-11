package de.unistuttgart.iste.meitrex.gamification_service.events.repository;

import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserProgressUpdatedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;


public interface IPersistentUserProgressUpdatedRepository extends JpaRepository<PersistentUserProgressUpdatedEvent, UUID> {

    List<PersistentUserProgressUpdatedEvent> findByStatus(PersistentEvent.Status status);
}
