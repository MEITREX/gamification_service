package de.unistuttgart.iste.meitrex.gamification_service.service.recommendation;

import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategoryRecommendation;
import de.unistuttgart.iste.meitrex.generated.dto.WidgetSettings;
import de.unistuttgart.iste.meitrex.generated.dto.WidgetSettingsInput;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public interface IWidgetRecommendationService {

    List<GamificationCategoryRecommendation> getUserWidgetRecommendations(@NotNull final UUID userId, @NotNull final UUID courseId);

    WidgetSettings getUserWidgetSettings(final UUID userId);

    WidgetSettings setUserWidgetSettings(final UUID userId, final WidgetSettingsInput input);

}
