package de.unistuttgart.iste.meitrex.gamification_service.recommendation;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Data
public class PreviousRecommendationsId implements Serializable {
    private final UUID userId;
    private final UUID courseId;
    private final RecommendationType recommendationType;

    public PreviousRecommendationsId(UUID userId, UUID courseId, RecommendationType recommendationType) {
        this.userId = userId;
        this.courseId = courseId;
        this.recommendationType = recommendationType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreviousRecommendationsId that = (PreviousRecommendationsId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(courseId, that.courseId) && recommendationType == that.recommendationType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, courseId, recommendationType);
    }
}
