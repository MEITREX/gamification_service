package de.unistuttgart.iste.meitrex.gamification_service.service.recommendation;

import de.unistuttgart.iste.meitrex.gamification_service.config.AdaptivityConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.PlayerHexadScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation.UserRecommendationScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.recommendation.RecommendationScoreRepository;
import de.unistuttgart.iste.meitrex.gamification_service.recommendation.RecommendationType;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import de.unistuttgart.iste.meitrex.generated.dto.RecommendationUserFeedback;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Component
public class DefaultRecommendationService implements IRecommendationService {

    private final RecommendationScoreRepository recommendationScoreRepository;
    private final AdaptivityConfiguration adaptivityConfiguration;

    public DefaultRecommendationService(@Autowired RecommendationScoreRepository recommendationScoreRepository,
                                        @Autowired AdaptivityConfiguration adaptivityConfiguration) {
        this.recommendationScoreRepository = Objects.requireNonNull(recommendationScoreRepository);
        this.adaptivityConfiguration = Objects.requireNonNull(adaptivityConfiguration);
    }

    @Override
    public LocalDateTime adjustFromUserFeedback(UUID userId, GamificationCategory recommendationType, RecommendationUserFeedback feedback) {
        final double adjustmentStrength = 0.05;
        final UserRecommendationScoreEntity userRecommendationScore = recommendationScoreRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId
                        + " does not have a recommendation score."));
        final double currentScore = userRecommendationScore.getScore(recommendationType);
        // calculate new next adjustment day period
        int currentNextAdjustmentInDays = userRecommendationScore.getNextAdjustmentRequestInDays(recommendationType);
        int newNextAdjustmentRequestInDays = switch (feedback) {
            case MORE_OFTEN, LESS_OFTEN -> (int) Math.ceil(currentNextAdjustmentInDays / 1.5);
            case JUST_RIGHT -> (int) Math.ceil(currentNextAdjustmentInDays * 1.5);
        };

        // clamp the period
        newNextAdjustmentRequestInDays = Math.max(
                newNextAdjustmentRequestInDays,
                adaptivityConfiguration.getWidgetRecommendationMinFeedbackRequestIntervalDays());

        newNextAdjustmentRequestInDays = Math.min(
                newNextAdjustmentRequestInDays,
                adaptivityConfiguration.getWidgetRecommendationMaxFeedbackRequestIntervalDays());

        // adjust the score value
        userRecommendationScore.setScore(
                recommendationType,
                switch (feedback) {
                    case MORE_OFTEN -> currentScore + adjustmentStrength;
                    case LESS_OFTEN -> currentScore - adjustmentStrength;
                    case JUST_RIGHT -> currentScore; // no change
                },
                LocalDateTime.now(),
                newNextAdjustmentRequestInDays
        );
        recommendationScoreRepository.save(userRecommendationScore);
        return userRecommendationScore.getLastAdjusted(recommendationType).plusDays(newNextAdjustmentRequestInDays);    }

}
