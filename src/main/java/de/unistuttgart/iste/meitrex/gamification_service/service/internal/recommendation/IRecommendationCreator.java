package de.unistuttgart.iste.meitrex.gamification_service.service.internal.recommendation;

import de.unistuttgart.iste.meitrex.gamification_service.recommendation.RecommendationType;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface IRecommendationCreator {


    /**
     * Makes a recommendation for the user based on their recommendation score and previous recommendations.
     * This method modifies the user's previous recommendations, thus influencing future recommendations.
     *
     * @param userId the ID of the user to make a recommendation for
     * @return the recommended type
     */
    GamificationCategory makeRecommendation(@NotNull final UUID userId, @NotNull UUID courseId, RecommendationType recommendationType);

    /**
     * Makes a recommendation for the user based on their recommendation score and previous recommendations.
     * If peek is true, the recommendation will not be saved to the user's previous recommendations, thus not
     * influencing future recommendations.
     *
     * @param userId the ID of the user to make a recommendation for
     * @param peek   if true, the recommendation will not be saved to the user's previous recommendations
     * @return the recommended type
     */
    GamificationCategory makeRecommendation(@NotNull final UUID userId, @NotNull final UUID courseId, RecommendationType recommendationType, boolean peek);

}