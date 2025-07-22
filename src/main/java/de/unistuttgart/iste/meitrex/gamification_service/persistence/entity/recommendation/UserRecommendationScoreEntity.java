package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
public class UserRecommendationScoreEntity implements IWithId<UUID> {
    @Id
    @Getter
    private final UUID userId;

    private static final double DEFAULT_SCORE = 1.0 / 8.0;

    @Embedded
    private RecommendationScoreEmbeddable socialization = new RecommendationScoreEmbeddable(DEFAULT_SCORE);
    @Embedded
    private RecommendationScoreEmbeddable assistance = new RecommendationScoreEmbeddable(DEFAULT_SCORE);
    @Embedded
    private RecommendationScoreEmbeddable immersion = new RecommendationScoreEmbeddable(DEFAULT_SCORE);
    @Embedded
    private RecommendationScoreEmbeddable riskReward = new RecommendationScoreEmbeddable(DEFAULT_SCORE);
    @Embedded
    private RecommendationScoreEmbeddable customization = new RecommendationScoreEmbeddable(DEFAULT_SCORE);
    @Embedded
    private RecommendationScoreEmbeddable progression = new RecommendationScoreEmbeddable(DEFAULT_SCORE);
    @Embedded
    private RecommendationScoreEmbeddable altruism = new RecommendationScoreEmbeddable(DEFAULT_SCORE);
    @Embedded
    private RecommendationScoreEmbeddable incentive = new RecommendationScoreEmbeddable(DEFAULT_SCORE);

    @Override
    public UUID getId() {
        return userId;
    }

    public double getScore(GamificationCategory category) {
        return getScoreEmbeddable(category).getScore();
    }

    public int getNextAdjustmentRequestInDays(GamificationCategory category) {
        return getScoreEmbeddable(category).getNextAdjustmentRequestInDays();
    }

    public LocalDateTime getNextAdjustmentRequestTime(GamificationCategory category) {
        RecommendationScoreEmbeddable scoreEmbeddable = getScoreEmbeddable(category);
        return scoreEmbeddable.getLastAdjusted().plusDays(scoreEmbeddable.getNextAdjustmentRequestInDays());
    }

    public LocalDateTime getLastAdjusted(GamificationCategory category) {
        return getScoreEmbeddable(category).getLastAdjusted();
    }

    /**
     * Sets the score for a specific recommendation type without modifying the last adjusted time or
     * next adjustment request day span. Normalization of the scores is performed after this operation.
     *
     * @param category the type of recommendation for which the score should be set
     * @param score the new score to set for the specified recommendation type
     */
    public void setScore(GamificationCategory category, double score) {
        setScoreNoNormalize(category, score);
        normalize();
    }

    public void setScore(GamificationCategory category,
                         double score,
                         LocalDateTime lastAdjusted,
                         int nextAdjustmentRequestInDays) {
        setScoreNoNormalize(category, score, lastAdjusted, nextAdjustmentRequestInDays);
        normalize();
    }

    public void setScores(Map<GamificationCategory, Double> scores) {
        scores.forEach(this::setScoreNoNormalize);
        normalize();
    }

    /**
     * Internal helper method to set the score for a specific recommendation type without normalization.
     * @param category the type of recommendation for which the score should be set
     * @param score the new score to set for the specified recommendation type
     */
    private void setScoreNoNormalize(GamificationCategory category, double score) {
        RecommendationScoreEmbeddable scoreEmbeddable = getScoreEmbeddable(category);

        RecommendationScoreEmbeddable newScore = new RecommendationScoreEmbeddable(
                score,
                scoreEmbeddable.getLastAdjusted(),
                scoreEmbeddable.getNextAdjustmentRequestInDays()
        );

        setScoreEmbeddable(category, newScore);
    }

    /**
     * Internal helper method to set the score for a specific recommendation type without normalization.
     * @param category the type of recommendation for which the score should be set
     * @param score the new score to set for the specified recommendation type
     * @param lastAdjusted the last adjusted time for the score
     * @param nextAdjustmentRequestInDays the number of days until the next adjustment request
     */
    private void setScoreNoNormalize(GamificationCategory category,
                                     double score,
                                     LocalDateTime lastAdjusted,
                                     int nextAdjustmentRequestInDays) {
        RecommendationScoreEmbeddable newScore = new RecommendationScoreEmbeddable(
                score,
                lastAdjusted,
                nextAdjustmentRequestInDays
        );

        setScoreEmbeddable(category, newScore);
    }

    public Map<GamificationCategory, Double> asMap() {
        return Arrays.stream(GamificationCategory.values())
                .collect(Collectors.toMap(
                        cat -> cat,
                        this::getScore
                ));
    }

    private RecommendationScoreEmbeddable getScoreEmbeddable(GamificationCategory category) {
        return switch (category) {
            case SOCIALIZATION -> socialization;
            case ASSISTANCE -> assistance;
            case IMMERSION -> immersion;
            case RISK_REWARD -> riskReward;
            case CUSTOMIZATION -> customization;
            case PROGRESSION -> progression;
            case ALTRUISM -> altruism;
            case INCENTIVE -> incentive;
        };
    }

    private void setScoreEmbeddable(GamificationCategory category, RecommendationScoreEmbeddable score) {
        switch (category) {
            case SOCIALIZATION -> socialization = score;
            case ASSISTANCE -> assistance = score;
            case IMMERSION -> immersion = score;
            case RISK_REWARD -> riskReward = score;
            case CUSTOMIZATION -> customization = score;
            case PROGRESSION -> progression = score;
            case ALTRUISM -> altruism = score;
            case INCENTIVE -> incentive = score;
        }
    }

    /**
     * Normalizes the scores of this entity in-place so that they sum up to 1.0.
     * This is useful for ensuring that the scores can be treated as probabilities.
     */
    private void normalize() {
        final double sum = asMap().values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();

        for(GamificationCategory cat : GamificationCategory.values()) {
            setScore(cat, getScore(cat) / sum);
        }
    }
}
