package de.unistuttgart.iste.meitrex.gamification_service.controller;

import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.gamification_service.service.recommendation.RecommendationService;
import de.unistuttgart.iste.meitrex.gamification_service.service.recommendation.WidgetRecommendationService;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategoryRecommendation;
import de.unistuttgart.iste.meitrex.generated.dto.RecommendationUserFeedback;
import de.unistuttgart.iste.meitrex.generated.dto.WidgetSettings;
import de.unistuttgart.iste.meitrex.generated.dto.WidgetSettingsInput;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RecommendationController {
    private final WidgetRecommendationService widgetRecommendationService;
    private final RecommendationService recommendationService;

    @QueryMapping("_internal_noauth_currentUserWidgetRecommendations")
    public List<GamificationCategoryRecommendation> currentUserWidgetRecommendations(
            @NotNull final UUID courseId,
            @ContextValue final LoggedInUser currentUser) {
        return widgetRecommendationService.getUserWidgetRecommendations(currentUser.getId(), courseId);
    }

    @MutationMapping
    public boolean sendRecommendationFeedback(final GamificationCategory category,
                                              final RecommendationUserFeedback feedback,
                                              @ContextValue final LoggedInUser currentUser) {
        recommendationService.adjustFromUserFeedback(currentUser.getId(), category, feedback);
        return true;
    }

    @QueryMapping
    public WidgetSettings currentUserWidgetSettings(@ContextValue final LoggedInUser currentUser) {
        return widgetRecommendationService.getUserWidgetSettings(currentUser.getId());
    }

    @MutationMapping
    public WidgetSettings setCurrentUserWidgetSettings(final WidgetSettingsInput settings,
                                                       @ContextValue final LoggedInUser currentUser) {
        return widgetRecommendationService.setUserWidgetSettings(currentUser.getId(), settings);
    }
}
