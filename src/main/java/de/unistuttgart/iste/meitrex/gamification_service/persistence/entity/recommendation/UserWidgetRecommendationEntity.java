package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation;

import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class UserWidgetRecommendationEntity {
    @Id
    @GeneratedValue
    private UUID id;

    private GamificationCategory category;

    public UserWidgetRecommendationEntity(GamificationCategory category) {
        this.category = category;
    }
}
