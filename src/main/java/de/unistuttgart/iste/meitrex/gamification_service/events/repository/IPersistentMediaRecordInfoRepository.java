package de.unistuttgart.iste.meitrex.gamification_service.events.repository;

import java.util.*;

import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.*;


public interface IPersistentMediaRecordInfoRepository extends IPersistentEventRepository<PersistentMediaRecordInfoEvent> {
    Optional<PersistentMediaRecordInfoEvent> findByMediaRecordId(UUID mediaRecordId);
}
