package de.unistuttgart.iste.meitrex.gamification_service.service.recommendation;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.PlayerHexadScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation.UserRecommendationScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation.UserPreviousRecommendationsEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.recommendation.RecommendationScoreRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.recommendation.UserPreviousRecommendationsRepository;
import de.unistuttgart.iste.meitrex.gamification_service.recommendation.PreviousRecommendationsId;
import de.unistuttgart.iste.meitrex.gamification_service.recommendation.RecommendationType;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import de.unistuttgart.iste.meitrex.generated.dto.RecommendationUserFeedback;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service which can make recommendations based on a user's recommendation score.
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class RecommendationService {
    private final RecommendationScoreRepository recommendationScoreRepository;
    private final UserPreviousRecommendationsRepository userPreviousRecommendationsRepository;

    public void initializeUserRecommendationScoreEmpty(final UUID userId) {
        recommendationScoreRepository.save(new UserRecommendationScoreEntity(userId));
    }

    public void initializeUserRecommendationScoreFromHexadScore(final PlayerHexadScoreEntity playerHexadScore) {
        final double[] hexadScores = {
                playerHexadScore.getFreeSpirit(),
                playerHexadScore.getPhilanthropist(),
                playerHexadScore.getAchiever(),
                playerHexadScore.getPlayer(),
                playerHexadScore.getSocialiser(),
                playerHexadScore.getDisruptor(),
        };

        final UserRecommendationScoreEntity recommendationScore =
                new UserRecommendationScoreEntity(playerHexadScore.getUserId());
        recommendationScore.setScores(
                Arrays.stream(GamificationCategory.values()).collect(Collectors.toMap(
                        cat -> cat,
                        cat -> hexadToRecommendationMapping(cat, hexadScores)
                ))
        );
        recommendationScoreRepository.save(recommendationScore);
    }

    public boolean isFeedbackRequestDue(@NotNull final UUID userId,
                                        @NotNull final GamificationCategory category) {
        final Optional<UserRecommendationScoreEntity> userRecommendationScore =
                recommendationScoreRepository.findById(userId);

        if(userRecommendationScore.isEmpty())
            return false;

        return userRecommendationScore.get().getNextAdjustmentRequestTime(category).isBefore(LocalDateTime.now());
    }

    /**
     * Makes a recommendation for the user based on their recommendation score and previous recommendations.
     * This method modifies the user's previous recommendations, thus influencing future recommendations.
     *
     * @param userId the ID of the user to make a recommendation for
     * @return the recommended type
     */
    public GamificationCategory makeRecommendation(@NotNull final UUID userId,
                                                   @NotNull UUID courseId,
                                                   RecommendationType recommendationType) {
        return makeRecommendation(userId, courseId, recommendationType, false);
    }

    /**
     * Makes a recommendation for the user based on their recommendation score and previous recommendations.
     * If peek is true, the recommendation will not be saved to the user's previous recommendations, thus not
     * influencing future recommendations.
     *
     * @param userId the ID of the user to make a recommendation for
     * @param peek   if true, the recommendation will not be saved to the user's previous recommendations
     * @return the recommended type
     */
    public GamificationCategory makeRecommendation(@NotNull final UUID userId,
                                                   @NotNull final UUID courseId,
                                                   RecommendationType recommendationType,
                                                   boolean peek) {
        final UserRecommendationScoreEntity userRecommendationScore = recommendationScoreRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId
                        + " does not have a recommendation score."));

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

    /**
     * Adjusts the recommendation score of a user based on their feedback.
     *
     * @param userId             the ID of the user to adjust the score for
     * @param recommendationType the type of recommendation to adjust the score for
     * @param feedback           the feedback provided by the user
     * @return the next date when a feedback request should be shown to the user
     */
    public LocalDateTime adjustFromUserFeedback(UUID userId,
                                                GamificationCategory recommendationType,
                                                RecommendationUserFeedback feedback) {
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
        newNextAdjustmentRequestInDays = Math.max(newNextAdjustmentRequestInDays, 8);
        newNextAdjustmentRequestInDays = Math.min(newNextAdjustmentRequestInDays, 30);

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

        return userRecommendationScore.getLastAdjusted(recommendationType).plusDays(newNextAdjustmentRequestInDays);
    }

    private UserPreviousRecommendationsEntity getOrCreateUserPreviousRecommendations(
            @NotNull final UUID userId,
            @NotNull final UUID courseId,
            @NotNull final RecommendationType recommendationType) {
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
    private Map<GamificationCategory, Double> clampRecommendationScoreProportional(
            final Map<GamificationCategory, Double> inScores,
            final double min) {
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
     * Helper function which calculates a specific recommendation score for a user based on their hexad scores.
     * Uses a polynomial mapping function to convert hexad scores to recommendation scores.
     *
     * @param category    the type of recommendation to calculate the score for
     * @param hexadScores the hexad scores of the user
     */
    private static double hexadToRecommendationMapping(final GamificationCategory category,
                                                       final double[] hexadScores) {
        if (hexadScores.length != 6) {
            throw new IllegalArgumentException("Scores array must have exactly 8 elements.");
        }

        float score = 0.0f;
        for (int i = 0; i < hexadScores.length; i++) {
            score += (float) (hexadScores[i] * correlations.get(category)[i]);
        }

        return (float) (0.5 * Math.pow(1 + score, 2));
    }

    /**
     * Helper function which calculates the expected number of recommendations for each category based on
     * the provided recommendation score and the total number of recommendations.
     *
     * @param recommendationScore the recommendation score of the user
     * @param recommendationCount the total number of recommendations to be made
     * @return a map of expected recommendation distribution
     */
    private Map<GamificationCategory, Double> expectedRecommendationDistribution(
            final Map<GamificationCategory, Double> recommendationScore,
            final int recommendationCount) {

        if (recommendationCount < 0) {
            throw new IllegalArgumentException("Recommendation count must be greater than 0.");
        }

        return Arrays.stream(GamificationCategory.values())
                .collect(Collectors.toMap(
                        cat -> cat,
                        cat -> recommendationCount * recommendationScore.get(cat)
                ));
    }

    /**
     * Correlations between hexad scores and recommendation scores.
     * As taken from
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


}
