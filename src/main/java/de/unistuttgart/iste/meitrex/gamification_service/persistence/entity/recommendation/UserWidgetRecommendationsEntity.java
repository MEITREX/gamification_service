package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Stores the widget recommendations for a user.
 */
@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class UserWidgetRecommendationsEntity implements IWithId<UUID> {
    /**
     * The ID of the user for whom the recommendations are generated.
     */
    @Id
    private UUID userId;

    /**
     * The user's widget recommendations for the current timeframe.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Setter
    private List<GamificationCategory> recommendations;

    /**
     * The time when the recommendations were generated.
     */
    @Setter
    private LocalDateTime generationTime;

    public UserWidgetRecommendationsEntity(UUID userId) {
        this.userId = userId;
    }

    @Override
    public UUID getId() {
        return userId;
    }
}
