package de.unistuttgart.iste.meitrex.gamification_service.service.internal.recommendation;

import java.util.*;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.*;


public interface IRecommendationInitializer {

    void initializeUserRecommendationScoreEmpty(final UUID userId);

    void initializeUserRecommendationScoreFromHexadScore(final PlayerHexadScoreEntity playerHexadScore);

}
