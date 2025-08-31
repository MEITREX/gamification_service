package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Data
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED, force = true)
final class RecommendationScoreEmbeddable {
    private final double score;
    @NotNull
    private final LocalDateTime lastAdjusted;
    private final int nextAdjustmentRequestInDays;

    public RecommendationScoreEmbeddable(double score, LocalDateTime lastAdjusted, int nextAdjustmentRequestInDays) {
        this.score = score;
        this.lastAdjusted = lastAdjusted;
        this.nextAdjustmentRequestInDays = nextAdjustmentRequestInDays;
    }

    public LocalDateTime getNextAdjustmentDate() {
        return lastAdjusted.plusDays(nextAdjustmentRequestInDays);
    }
}
