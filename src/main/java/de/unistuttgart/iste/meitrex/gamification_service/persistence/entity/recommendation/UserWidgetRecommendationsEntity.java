package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class UserWidgetRecommendationsEntity implements IWithId<UUID> {

    @Id
    private UUID userId;

    @OneToMany
    @Setter
    private List<UserWidgetRecommendationEntity> recommendations;

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
