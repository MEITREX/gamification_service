package de.unistuttgart.iste.meitrex.gamification_service.events.repository;

import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IPersistentEventStatusRepository  extends JpaRepository<PersistentEvent.PersistentEventStatus, PersistentEvent.PersistentEventStatusID> {

}
