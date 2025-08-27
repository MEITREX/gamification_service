package de.unistuttgart.iste.meitrex.gamification_service.events.repository;

import de.unistuttgart.iste.meitrex.common.event.ContentProgressedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentContentProgressedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentForumActivityEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface IPersistentContentProgressedRepository extends IPersistentEventRepository<PersistentContentProgressedEvent> {

}
