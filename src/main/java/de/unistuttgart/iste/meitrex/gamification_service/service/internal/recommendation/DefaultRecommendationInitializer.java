package de.unistuttgart.iste.meitrex.gamification_service.service.internal.recommendation;

import de.unistuttgart.iste.meitrex.gamification_service.config.AdaptivityConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.PlayerHexadScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation.UserRecommendationScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.recommendation.RecommendationScoreRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.recommendation.UserPreviousRecommendationsRepository;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class DefaultRecommendationInitializer implements IRecommendationInitializer {

    /**
     * Correlations between hexad scores and recommendation scores. As taken from
     * G. F. Tondello, A. Mora, L. E. Nacke. “Elements of Gameful Design Emerging from
     * User Preferences”. In: Proceedings of the Annual Symposium on Computer-Human
     * Interaction in Play. CHI PLAY ’17. Amsterdam, The Netherlands: Association
     * for Computing Machinery, 2017, 129–142. isbn: 9781450348980. doi: 10.1145/
     * 3116595.3116627
     */
    private static final Map<GamificationCategory, float[]> correlations = Map.of(
            GamificationCategory.SOCIALIZATION, new float[]{0.003f, 0.104f, 0.283f, 0.263f, 0.480f, 0.125f},
            GamificationCategory.ASSISTANCE,    new float[]{0.126f, 0.112f, -.015f, -.016f, 0.190f, 0.025f},
            GamificationCategory.IMMERSION,     new float[]{0.406f, 0.170f, 0.394f, 0.053f, 0.100f, 0.165f},
            GamificationCategory.RISK_REWARD,   new float[]{0.120f, 0.084f, 0.361f, 0.247f, 0.026f, 0.183f},
            GamificationCategory.CUSTOMIZATION, new float[]{0.117f, -.019f, -.070f, 0.130f, -.069f, 0.006f},
            GamificationCategory.PROGRESSION,   new float[]{0.013f, 0.170f, 0.186f, 0.104f, 0.072f, 0.084f},
            GamificationCategory.ALTRUISM,      new float[]{0.149f, 0.377f, 0.179f, 0.143f, 0.227f, 0.093f},
            GamificationCategory.INCENTIVE,     new float[]{0.030f, 0.024f, 0.056f, 0.351f, 0.103f, 0.003f}
    );

    // Validation

    private static void assureHasUser(PlayerHexadScoreEntity playerHexadScore) {
        if(Objects.isNull(playerHexadScore.getUser())) {
            throw new IllegalArgumentException();
        }
    }

    // Helpers

    /**
     * Helper function which calculates a specific recommendation score for a user based on their hexad scores.
     * Uses a polynomial mapping function to convert hexad scores to recommendation scores.
     *
     * @param category    the type of recommendation to calculate the score for
     * @param hexadScores the hexad scores of the user
     */
    private static double hexadToRecommendationMapping(final GamificationCategory category, final double[] hexadScores) {
        if (hexadScores.length != 6) {
            throw new IllegalArgumentException("Scores array must have exactly 8 elements.");
        }

        float score = 0.0f;
        for (int i = 0; i < hexadScores.length; i++) {
            score += (float) (hexadScores[i] * correlations.get(category)[i]);
        }

        return (float) (0.5 * Math.pow(1 + score, 2));
    }


    private final RecommendationScoreRepository recommendationScoreRepository;
    private final AdaptivityConfiguration adaptivityConfiguration;

    @Override
    public UserRecommendationScoreEntity initializeRecommendationScoreForUser(final UserEntity userEntity) {
        UserRecommendationScoreEntity entity = userEntity.getPlayerHexadScore() == null
                ? initializeUserRecommendationScoreEmpty(userEntity.getId())
                : initializeUserRecommendationScoreFromHexadScore(userEntity.getPlayerHexadScore());
        return recommendationScoreRepository.save(entity);
    }

    private UserRecommendationScoreEntity initializeUserRecommendationScoreEmpty(UUID userId) {
        return recommendationScoreRepository.save(new UserRecommendationScoreEntity(
                userId, adaptivityConfiguration.getWidgetRecommendationDefaultFeedbackRequestIntervalDays()));
    }

    private UserRecommendationScoreEntity initializeUserRecommendationScoreFromHexadScore(PlayerHexadScoreEntity playerHexadScore) {
        assureHasUser(playerHexadScore);
        final double[] hexadScores = {
                playerHexadScore.getFreeSpirit(),
                playerHexadScore.getPhilanthropist(),
                playerHexadScore.getAchiever(),
                playerHexadScore.getPlayer(),
                playerHexadScore.getSocialiser(),
                playerHexadScore.getDisruptor(),
        };
        final UserEntity user = playerHexadScore.getUser();
        final UserRecommendationScoreEntity recommendationScore =
                new UserRecommendationScoreEntity(user.getId(),
                        adaptivityConfiguration.getWidgetRecommendationDefaultFeedbackRequestIntervalDays());
        recommendationScore.setScores(
                Arrays.stream(GamificationCategory.values()).collect(Collectors.toMap(
                        cat -> cat,
                        cat -> hexadToRecommendationMapping(cat, hexadScores)
                ))
        );
        return recommendationScoreRepository.save(recommendationScore);
    }
}
