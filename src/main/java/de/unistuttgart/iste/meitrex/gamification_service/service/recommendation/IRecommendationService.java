package de.unistuttgart.iste.meitrex.gamification_service.service.recommendation;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.PlayerHexadScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation.UserPreviousRecommendationsEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation.UserRecommendationScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.recommendation.RecommendationType;
import de.unistuttgart.iste.meitrex.generated.dto.*;
import org.jetbrains.annotations.NotNull;

public interface IRecommendationService {

    /**
     * Adjusts the recommendation score of a user based on their feedback.
     *
     * @param userId             the ID of the user to adjust the score for
     * @param recommendationType the type of recommendation to adjust the score for
     * @param feedback           the feedback provided by the user
     * @return the next date when a feedback request should be shown to the user
     */
    LocalDateTime adjustFromUserFeedback(UUID userId, GamificationCategory recommendationType, RecommendationUserFeedback feedback);

}
