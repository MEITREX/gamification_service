package de.unistuttgart.iste.meitrex.gamification_service.events.repository;

import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentForumActivityEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface IPersistentForumActivityRepository extends IPersistentEventRepository<PersistentForumActivityEvent> {

}
