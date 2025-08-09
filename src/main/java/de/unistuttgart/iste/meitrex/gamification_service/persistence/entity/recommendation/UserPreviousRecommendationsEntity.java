package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.gamification_service.recommendation.PreviousRecommendationsId;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Entity to keep track of the number of recommendations given to a user in each gamification category.
 * This is used to avoid recommending the same category too often.
 */
@Entity
@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
@RequiredArgsConstructor
public class UserPreviousRecommendationsEntity implements IWithId<PreviousRecommendationsId> {
    @Id
    private final PreviousRecommendationsId id;

    private int socializationCount = 0;
    private int assistanceCount = 0;
    private int immersionCount = 0;
    private int riskRewardCount = 0;
    private int customizationCount = 0;
    private int progressionCount = 0;
    private int altruismCount = 0;
    private int incentiveCount = 0;

    public int getCountForCategory(GamificationCategory category) {
        return switch (category) {
            case SOCIALIZATION -> socializationCount;
            case ASSISTANCE -> assistanceCount;
            case IMMERSION -> immersionCount;
            case RISK_REWARD -> riskRewardCount;
            case CUSTOMIZATION -> customizationCount;
            case PROGRESSION -> progressionCount;
            case ALTRUISM -> altruismCount;
            case INCENTIVE -> incentiveCount;
        };
    }

    public void incrementCountForCategory(GamificationCategory category) {
        switch (category) {
            case SOCIALIZATION -> socializationCount++;
            case ASSISTANCE -> assistanceCount++;
            case IMMERSION -> immersionCount++;
            case RISK_REWARD -> riskRewardCount++;
            case CUSTOMIZATION -> customizationCount++;
            case PROGRESSION -> progressionCount++;
            case ALTRUISM -> altruismCount++;
            case INCENTIVE -> incentiveCount++;
        }
    }

    public int getTotalRecommendationCount() {
        return socializationCount + assistanceCount + immersionCount + riskRewardCount +
               customizationCount + progressionCount + altruismCount + incentiveCount;
    }

    @Override
    public PreviousRecommendationsId getId() {
        return id;
    }
}
