package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor(force = true)
public class UserRecommendationScoreEntity implements IWithId<UUID> {
    @Id
    @Getter
    private final UUID userId;

    private static final double DEFAULT_SCORE = 1.0 / 8.0;

    /*
        * The scores for the different recommendation types.
        * The @AttributeOverrides are necessary because otherwise Hibernate would try to use the same column names
        * for the fields of all scores, which would lead to conflicts.
     */

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "score", column = @Column(name = "socialization_score")),
            @AttributeOverride(name = "lastAdjusted", column = @Column(name = "socialization_last_adjusted")),
            @AttributeOverride(name = "nextAdjustmentRequestInDays", column = @Column(name = "socialization_next_adjustment"))
    })
    private RecommendationScoreEmbeddable socialization;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "score", column = @Column(name = "assistance_score")),
            @AttributeOverride(name = "lastAdjusted", column = @Column(name = "assistance_last_adjusted")),
            @AttributeOverride(name = "nextAdjustmentRequestInDays", column = @Column(name = "assistance_next_adjustment"))
    })
    private RecommendationScoreEmbeddable assistance;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "score", column = @Column(name = "immersion_score")),
            @AttributeOverride(name = "lastAdjusted", column = @Column(name = "immersion_last_adjusted")),
            @AttributeOverride(name = "nextAdjustmentRequestInDays", column = @Column(name = "immersion_next_adjustment"))
    })
    private RecommendationScoreEmbeddable immersion;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "score", column = @Column(name = "risk_reward_score")),
            @AttributeOverride(name = "lastAdjusted", column = @Column(name = "risk_reward_last_adjusted")),
            @AttributeOverride(name = "nextAdjustmentRequestInDays", column = @Column(name = "risk_reward_next_adjustment"))
    })
    private RecommendationScoreEmbeddable riskReward;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "score", column = @Column(name = "customization_score")),
            @AttributeOverride(name = "lastAdjusted", column = @Column(name = "customization_last_adjusted")),
            @AttributeOverride(name = "nextAdjustmentRequestInDays", column = @Column(name = "customization_next_adjustment"))
    })
    private RecommendationScoreEmbeddable customization;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "score", column = @Column(name = "progression_score")),
            @AttributeOverride(name = "lastAdjusted", column = @Column(name = "progression_last_adjusted")),
            @AttributeOverride(name = "nextAdjustmentRequestInDays", column = @Column(name = "progression_next_adjustment"))
    })
    private RecommendationScoreEmbeddable progression;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "score", column = @Column(name = "altruism_score")),
            @AttributeOverride(name = "lastAdjusted", column = @Column(name = "altruism_last_adjusted")),
            @AttributeOverride(name = "nextAdjustmentRequestInDays", column = @Column(name = "altruism_next_adjustment"))
    })
    private RecommendationScoreEmbeddable altruism;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "score", column = @Column(name = "incentive_score")),
            @AttributeOverride(name = "lastAdjusted", column = @Column(name = "incentive_last_adjusted")),
            @AttributeOverride(name = "nextAdjustmentRequestInDays", column = @Column(name = "incentive_next_adjustment"))
    })
    private RecommendationScoreEmbeddable incentive;

    public UserRecommendationScoreEntity(final UUID userId, final int defaultFeedbackRequestIntervalDays) {
        this.userId = userId;

        this.socialization = new RecommendationScoreEmbeddable(
                DEFAULT_SCORE, LocalDateTime.now(), defaultFeedbackRequestIntervalDays);
        this.assistance = new RecommendationScoreEmbeddable(
                DEFAULT_SCORE, LocalDateTime.now(), defaultFeedbackRequestIntervalDays);
        this.immersion = new RecommendationScoreEmbeddable(
                DEFAULT_SCORE, LocalDateTime.now(), defaultFeedbackRequestIntervalDays);
        this.riskReward = new RecommendationScoreEmbeddable(
                DEFAULT_SCORE, LocalDateTime.now(), defaultFeedbackRequestIntervalDays);
        this.customization = new RecommendationScoreEmbeddable(
                DEFAULT_SCORE, LocalDateTime.now(), defaultFeedbackRequestIntervalDays);
        this.progression = new RecommendationScoreEmbeddable(
                DEFAULT_SCORE, LocalDateTime.now(), defaultFeedbackRequestIntervalDays);
        this.altruism = new RecommendationScoreEmbeddable(
                DEFAULT_SCORE, LocalDateTime.now(), defaultFeedbackRequestIntervalDays);
        this.incentive = new RecommendationScoreEmbeddable(
                DEFAULT_SCORE, LocalDateTime.now(), defaultFeedbackRequestIntervalDays);
    }

    @Override
    public UUID getId() {
        return userId;
    }

    /**
     * @return Gets a score of a specific recommendation category.
     */
    public double getScore(GamificationCategory category) {
        return getScoreEmbeddable(category).getScore();
    }

    /**
     * @return Returns how many days after the last adjustment request the next adjustment request for a specific recommendation
     * category should be shown.
     */
    public int getNextAdjustmentRequestInDays(GamificationCategory category) {
        return getScoreEmbeddable(category).getNextAdjustmentRequestInDays();
    }

    /**
     * @return Returns the time when the next adjustment request for a specific recommendation category should be shown.
     */
    public LocalDateTime getNextAdjustmentRequestTime(GamificationCategory category) {
        RecommendationScoreEmbeddable scoreEmbeddable = getScoreEmbeddable(category);
        return scoreEmbeddable.getLastAdjusted().plusDays(scoreEmbeddable.getNextAdjustmentRequestInDays());
    }

    /**
     * @return Returns the time when the score for a specific recommendation category was last adjusted.
     */
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

    /**
     * Sets the score for a specific recommendation type, including the last adjusted time and
     * next adjustment request day span.
     * This method also normalizes the scores after setting the new score.
     * @param category the type of recommendation for which the score should be set
     * @param score the new score to set for the specified recommendation type
     * @param lastAdjusted the last adjusted time for the score
     * @param nextAdjustmentRequestInDays the number of days until the next adjustment request
     */
    public void setScore(GamificationCategory category,
                         double score,
                         LocalDateTime lastAdjusted,
                         int nextAdjustmentRequestInDays) {
        setScoreNoNormalize(category, score, lastAdjusted, nextAdjustmentRequestInDays);
        normalize();
    }

    /**
     * Sets the scores for multiple recommendation types at once.
     * @param scores a map containing the scores for each recommendation type
     */
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

    /**
     * Convenience method to get the scores as a map.
     */
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
            setScoreNoNormalize(cat, getScore(cat) / sum);
        }
    }
}
