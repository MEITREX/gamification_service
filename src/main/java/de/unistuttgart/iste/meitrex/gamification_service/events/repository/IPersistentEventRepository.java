package de.unistuttgart.iste.meitrex.gamification_service.events.repository;

import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserProgressUpdatedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface IPersistentEventRepository extends JpaRepository<PersistentEvent, UUID> {

    Optional<PersistentUserProgressUpdatedEvent> findBySequenceNo(Long msgSeqNo);

}
