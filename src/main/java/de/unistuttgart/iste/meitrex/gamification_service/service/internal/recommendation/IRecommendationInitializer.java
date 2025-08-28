package de.unistuttgart.iste.meitrex.gamification_service.service.internal.recommendation;

import java.util.*;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation.UserRecommendationScoreEntity;


public interface IRecommendationInitializer {

    UserRecommendationScoreEntity initializeRecommendationScoreForUser(final UserEntity userEntity);
}
