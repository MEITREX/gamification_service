package de.unistuttgart.iste.meitrex.gamification_service.service.internal.recommendation;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation.UserRecommendationScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.recommendation.RecommendationScoreRepository;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
class DefaultFeedbackDeadline implements IFeedbackRequestDeadline {

    private final RecommendationScoreRepository recommendationScoreRepository;

    public DefaultFeedbackDeadline(@Autowired  RecommendationScoreRepository recommendationScoreRepository) {
        this.recommendationScoreRepository = Objects.requireNonNull(recommendationScoreRepository);
    }

    @Override
    public boolean isFeedbackRequestDue(@NotNull UUID userId, @NotNull GamificationCategory category) {
        final Optional<UserRecommendationScoreEntity> userRecommendationScore = recommendationScoreRepository.findById(userId);
        return userRecommendationScore.map(userRecommendationScoreEntity -> userRecommendationScoreEntity
                .getNextAdjustmentRequestTime(category)
                .isBefore(LocalDateTime.now())).orElse(false);
    }

}
