package de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.recommendation;

import de.unistuttgart.iste.meitrex.common.persistence.MeitrexRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation.UserRecommendationScoreEntity;

import java.util.UUID;

public interface RecommendationScoreRepository extends MeitrexRepository<UserRecommendationScoreEntity, UUID> {
}
