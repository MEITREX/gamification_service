package de.unistuttgart.iste.meitrex.gamification_service.events.repository;

import java.util.*;

import org.springframework.data.jpa.repository.*;

import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.*;
import org.springframework.stereotype.Repository;

@Repository("default")
public interface IPersistentEventRepository<T extends PersistentEvent> extends JpaRepository<T, UUID> {
    Optional<PersistentEvent> findByMsgSequenceNo(Long msgSeqNo);
}
