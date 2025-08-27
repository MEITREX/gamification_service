package de.unistuttgart.iste.meitrex.gamification_service.persistence.repository;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.PlayerHexadScoreEntity;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlayerHexadScoreRepository extends JpaRepository<PlayerHexadScoreEntity, Long> {
    Optional<PlayerHexadScoreEntity> findByUserId(UUID userId);
}
