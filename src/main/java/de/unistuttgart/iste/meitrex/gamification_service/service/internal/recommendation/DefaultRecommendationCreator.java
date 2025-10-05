package de.unistuttgart.iste.meitrex.gamification_service.service.internal.recommendation;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation.UserPreviousRecommendationsEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation.UserRecommendationScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.recommendation.RecommendationScoreRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.recommendation.UserPreviousRecommendationsRepository;
import de.unistuttgart.iste.meitrex.gamification_service.recommendation.PreviousRecommendationsId;
import de.unistuttgart.iste.meitrex.gamification_service.recommendation.RecommendationType;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class DefaultRecommendationCreator implements IRecommendationCreator {

    // Dependencies

    private final RecommendationScoreRepository recommendationScoreRepository;
    private final UserPreviousRecommendationsRepository userPreviousRecommendationsRepository;
    private final IRecommendationInitializer recommendationInitializer;
    private final IUserCreator userCreator;


    // Interface Implementation

    @Override
    public GamificationCategory makeRecommendation(@NotNull UUID userId, @NotNull UUID courseId, RecommendationType recommendationType) {
        return makeRecommendation(userId, courseId, recommendationType, false);
    }

    @Override
    public GamificationCategory makeRecommendation(@NotNull UUID userId, @NotNull UUID courseId, RecommendationType recommendationType, boolean peek) {
        final UserEntity userEntity = userCreator.fetchOrCreate(userId);

        final UserRecommendationScoreEntity userRecommendationScore = recommendationScoreRepository.findById(userId)
                .orElseGet(() -> recommendationInitializer.initializeRecommendationScoreForUser(userEntity));



        final UserPreviousRecommendationsEntity userPreviousRecommendations =
                getOrCreateUserPreviousRecommendations(userId, courseId, recommendationType);

        Map<GamificationCategory, Double> recommendationScores =
                clampRecommendationScoreProportional(userRecommendationScore.asMap(),
                        0.05);

        // get how many recommendations for each type we expect the user to have based on their recommendation score
        final Map<GamificationCategory, Double> expectedDistribution = expectedRecommendationDistribution(
                recommendationScores,
                userPreviousRecommendations.getTotalRecommendationCount() + 1);

        // get how many recommendations for each type the user has actually received
        final Map<GamificationCategory, Double> actualDistribution = Arrays.stream(GamificationCategory.values())
                .collect(Collectors.toMap(
                        cat -> cat,
                        cat -> (double) userPreviousRecommendations.getCountForCategory(cat)
                ));

        // calculate the difference between the expected and actual number of recommendations for each type
        final Map<GamificationCategory, Double> diffDistribution = new HashMap<>();
        for (GamificationCategory cat : GamificationCategory.values()) {
            final double expected = expectedDistribution.get(cat);
            final double actual = actualDistribution.get(cat);
            diffDistribution.put(cat, expected - actual);
        }

        // get the type which has the highest difference between expected and actual recommendations
        GamificationCategory recommendation = diffDistribution.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElseThrow(() -> new IllegalStateException("No recommendation type found."))
                .getKey();

        if(!peek) {
            userPreviousRecommendations.incrementCountForCategory(recommendation);
            userPreviousRecommendationsRepository.save(userPreviousRecommendations);
        }

        return recommendation;
    }


    private UserPreviousRecommendationsEntity getOrCreateUserPreviousRecommendations(@NotNull final UUID userId, @NotNull final UUID courseId, @NotNull final RecommendationType recommendationType) {
        return userPreviousRecommendationsRepository
                .findById(new PreviousRecommendationsId(userId, courseId, recommendationType))
                .orElseGet(() -> {
                    final UserPreviousRecommendationsEntity entity =
                            new UserPreviousRecommendationsEntity(
                                    new PreviousRecommendationsId(userId, courseId, recommendationType));
                    userPreviousRecommendationsRepository.save(entity);
                    return entity;
                });
    }

    /**
     * Helper method which clamps recommendation scores to a minimum value while ensuring proportionality between
     * the other, non-clamped scores.
     * This ensures that all scores are above the minimum and that they sum up to 1.0.
     *
     * @param inScores the input scores to clamp
     * @param min      the minimum value for the scores
     * @return a map of clamped scores
     */
    private Map<GamificationCategory, Double> clampRecommendationScoreProportional(final Map<GamificationCategory, Double> inScores, final double min) {
        final Map<GamificationCategory, Double> scores = new HashMap<>(inScores);
        while (scores.values().stream().anyMatch(x -> x < min)) {
            double adjustableTotal = 0;
            for (GamificationCategory cat : GamificationCategory.values()) {
                if (scores.get(cat) < min) {
                    scores.put(cat, min);
                } else {
                    adjustableTotal += scores.get(cat);
                }
            }
            // sum up our total value after we clamped the scores under the minimum
            final double sum = scores.values().stream().mapToDouble(Double::doubleValue).sum();
            // how much we "overshot" the maximum total of 1.0 (we want all score to sum up to 1.0 to use them as
            // probabilities)
            final double delta = sum - 1.0;
            // now we'll distribute the "too much" score we have proportionally onto the scores that are
            // above the minimum
            for (GamificationCategory cat : GamificationCategory.values()) {
                double score = scores.get(cat);
                if (score > min) {
                    score = score - delta * (score / adjustableTotal);
                    scores.put(cat, score);
                }
            }
            // if we have a lot of scores close to the minimum, it might happen that we again have scores below
            // the minimum after adjustment, so we repeat the process until all scores are above the minimum
        }
        return scores;
    }

    /**
     * Helper function which calculates the expected number of recommendations for each category based on
     * the provided recommendation score and the total number of recommendations.
     *
     * @param recommendationScore the recommendation score of the user
     * @param recommendationCount the total number of recommendations to be made
     * @return a map of expected recommendation distribution
     */
    private Map<GamificationCategory, Double> expectedRecommendationDistribution(final Map<GamificationCategory, Double> recommendationScore, final int recommendationCount) {

        if (recommendationCount < 0) {
            throw new IllegalArgumentException("Recommendation count must be greater than 0.");
        }

        return Arrays.stream(GamificationCategory.values())
                .collect(Collectors.toMap(
                        cat -> cat,
                        cat -> recommendationCount * recommendationScore.get(cat)
                ));
    }

}
