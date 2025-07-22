package de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.recommendation;

import de.unistuttgart.iste.meitrex.common.persistence.MeitrexRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation.UserPreviousRecommendationsEntity;
import de.unistuttgart.iste.meitrex.gamification_service.recommendation.PreviousRecommendationsId;

import java.util.UUID;

public interface UserPreviousRecommendationsRepository extends MeitrexRepository<UserPreviousRecommendationsEntity, PreviousRecommendationsId> {
}
