package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;

@Data
@Entity
@NoArgsConstructor(force = true, access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
public class UserWidgetSettingsEntity implements IWithId<UUID> {
    @Id
    private final UUID userId;

    /**
     * Number of recommendation widgets to show to the user at the same time.
     */
    private int numberOfRecommendations = 2;
    /**
     * Interval in hours after which the recommendations should be refreshed.
     */
    private int recommendationRefreshInterval = 12;

    @Override
    public UUID getId() {
        return userId;
    }
}
