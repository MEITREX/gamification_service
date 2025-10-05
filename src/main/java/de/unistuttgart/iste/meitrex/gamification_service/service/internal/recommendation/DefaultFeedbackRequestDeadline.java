package de.unistuttgart.iste.meitrex.gamification_service.service.internal.recommendation;

import de.unistuttgart.iste.meitrex.gamification_service.keycloak.IUserConfigurationProvider;
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
class DefaultFeedbackRequestDeadline implements IFeedbackRequestDeadline {

    private final IUserConfigurationProvider userConfigurationProvider;

    private final RecommendationScoreRepository recommendationScoreRepository;

    public DefaultFeedbackRequestDeadline(@Autowired IUserConfigurationProvider userConfigurationProvider, @Autowired  RecommendationScoreRepository recommendationScoreRepository) {
        this.userConfigurationProvider = Objects.requireNonNull(userConfigurationProvider);
        this.recommendationScoreRepository = Objects.requireNonNull(recommendationScoreRepository);
    }

    @Override
    public boolean isFeedbackRequestDue(@NotNull UUID userId, @NotNull GamificationCategory category) {

        if(userConfigurationProvider.isAdaptiveGamificationDisabled(userId)) {
            return false;
        }

        final Optional<UserRecommendationScoreEntity> userRecommendationScore = recommendationScoreRepository.findById(userId);
        return userRecommendationScore.map(userRecommendationScoreEntity -> userRecommendationScoreEntity
                .getNextAdjustmentRequestTime(category)
                .isBefore(LocalDateTime.now())).orElse(false);
    }

}
