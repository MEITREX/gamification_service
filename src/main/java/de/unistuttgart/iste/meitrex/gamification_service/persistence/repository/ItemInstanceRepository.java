package de.unistuttgart.iste.meitrex.gamification_service.persistence.repository;

import de.unistuttgart.iste.meitrex.common.persistence.MeitrexRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemInstanceEntity;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ItemInstanceRepository extends MeitrexRepository<ItemInstanceEntity, UUID> {
}
