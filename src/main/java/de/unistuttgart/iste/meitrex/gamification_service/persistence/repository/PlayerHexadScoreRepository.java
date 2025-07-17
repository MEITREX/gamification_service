package de.unistuttgart.iste.meitrex.gamification_service.persistence.repository;

import de.unistuttgart.iste.meitrex.common.persistence.MeitrexRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.PlayerHexadScoreEntity;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerHexadScoreRepository extends MeitrexRepository<PlayerHexadScoreEntity, Long> {
    Optional<PlayerHexadScoreEntity> findByUserId(UUID userId);
}
