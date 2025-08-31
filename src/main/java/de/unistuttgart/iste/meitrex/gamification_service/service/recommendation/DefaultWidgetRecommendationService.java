package de.unistuttgart.iste.meitrex.gamification_service.service.recommendation;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation.UserWidgetRecommendationsEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation.UserWidgetSettingsEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.recommendation.RecommendationScoreRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.recommendation.UserPreviousRecommendationsRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.recommendation.UserWidgetRecommendationsRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.recommendation.UserWidgetSettingsRepository;
import de.unistuttgart.iste.meitrex.gamification_service.recommendation.RecommendationType;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.recommendation.IFeedbackRequestDeadline;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.recommendation.IRecommendationCreator;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategoryRecommendation;
import de.unistuttgart.iste.meitrex.generated.dto.WidgetSettings;
import de.unistuttgart.iste.meitrex.generated.dto.WidgetSettingsInput;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

@Component
public class DefaultWidgetRecommendationService implements IWidgetRecommendationService{

    private static boolean isNewWidgetRecommendationDue(final UserWidgetRecommendationsEntity entity, final WidgetSettings settings) {
        LocalDateTime previousTime = entity.getGenerationTime();
        LocalDateTime nextGenerationDue = LocalDate.now().atStartOfDay();
        while(nextGenerationDue.isBefore(previousTime) ||
                nextGenerationDue.isEqual(previousTime)) {
            nextGenerationDue = nextGenerationDue.plusHours(settings.getRecommendationRefreshInterval());
        }
        return nextGenerationDue.isBefore(LocalDateTime.now());
    }

    // Dependencies

    private final UserWidgetRecommendationsRepository widgetRecommendationsRepository;

    private final UserWidgetSettingsRepository widgetSettingsRepository;

    private final ModelMapper modelMapper;

    private final IFeedbackRequestDeadline feedbackRequestDeadline;

    private final IRecommendationCreator recommendationCreator;

    // Constructors

    public DefaultWidgetRecommendationService(
            @Autowired IRecommendationCreator recommendationCreator,
            @Autowired UserWidgetRecommendationsRepository widgetRecommendationsRepository,
            @Autowired UserWidgetSettingsRepository widgetSettingsRepository,
            @Autowired IFeedbackRequestDeadline feedbackRequestDeadline,
            @Autowired ModelMapper modelMapper
    ) {
        this.widgetRecommendationsRepository = Objects.requireNonNull(widgetRecommendationsRepository);
        this.widgetSettingsRepository = Objects.requireNonNull(widgetSettingsRepository);
        this.modelMapper = Objects.requireNonNull(modelMapper);
        this.recommendationCreator = Objects.requireNonNull(recommendationCreator);
        this.feedbackRequestDeadline = Objects.requireNonNull(feedbackRequestDeadline);
    }

    // Interface Implementation

    @Override
    public List<GamificationCategoryRecommendation> getUserWidgetRecommendations(@NotNull UUID userId, @NotNull UUID courseId) {
        WidgetSettings settings = getUserWidgetSettings(userId);
        if(settings.getNumberOfRecommendations() == 0)
            return List.of();
        Optional<UserWidgetRecommendationsEntity> recommendationsEntity =
                widgetRecommendationsRepository.findById(userId);
        // check if we already have some recommendations for the user which aren't too old, otherwise generate new ones
        if(recommendationsEntity.isEmpty() || isNewWidgetRecommendationDue(recommendationsEntity.get(), settings)) {
            // create our data holder entity if we don't have one yet (basically happens the first time a user
            // requests recommendations)
            if (recommendationsEntity.isEmpty())
                recommendationsEntity = Optional.of(new UserWidgetRecommendationsEntity(userId));

            // generate new recommendations
            List<GamificationCategory> recommendations = generateWidgetRecommendations(settings, userId, courseId);

            recommendationsEntity.get().setRecommendations(recommendations);
            // calculate generation time
            // we actually do not want to save the current time, but the last time of the interval which is
            // a division of 24 hours. E.g. if it's currently 14:30, and our interval is 6 hours, we want to save
            // the time 12:00, so that we always generate new recommendations at the correct time
            LocalDateTime generationTime = LocalDate.now().atStartOfDay();
            while(generationTime.plusHours(settings.getRecommendationRefreshInterval()).isBefore(LocalDateTime.now()) ||
                    generationTime.plusHours(settings.getRecommendationRefreshInterval()).isEqual(LocalDateTime.now())) {
                generationTime = generationTime.plusHours(settings.getRecommendationRefreshInterval());
            }
            recommendationsEntity.get().setGenerationTime(generationTime);
            // save
            widgetRecommendationsRepository.save(recommendationsEntity.get());
        }
        return recommendationsEntity.get().getRecommendations().stream()
                .map(x ->
                        new GamificationCategoryRecommendation(
                                x,
                                feedbackRequestDeadline.isFeedbackRequestDue(userId, x)))
                .toList();
    }

    @Override
    public WidgetSettings getUserWidgetSettings(UUID userId) {
        Optional<UserWidgetSettingsEntity> settingsEntity = widgetSettingsRepository.findById(userId);
        if(settingsEntity.isEmpty()) {
            settingsEntity = Optional.of(createDefaultUserWidgetSettings(userId));
        }
        return modelMapper.map(settingsEntity.get(), WidgetSettings.class);
    }

    @Override
    public WidgetSettings setUserWidgetSettings(UUID userId, WidgetSettingsInput input) {
        Optional<UserWidgetSettingsEntity> settingsEntity = widgetSettingsRepository.findById(userId);
        if(settingsEntity.isEmpty()) {
            settingsEntity = Optional.of(createDefaultUserWidgetSettings(userId));
        }
        UserWidgetSettingsEntity entity = settingsEntity.get();
        entity.setNumberOfRecommendations(input.getNumberOfRecommendations());
        entity.setRecommendationRefreshInterval(input.getRecommendationRefreshInterval());
        widgetSettingsRepository.save(entity);
        return modelMapper.map(entity, WidgetSettings.class);
    }

    /**
     * Helper method which generates new widget recommendations for a user. Makes sure that no duplicate recommendations
     * are generated. The number of recommendations is determined by the WidgetSettings. However, fewer recommendations
     * can be returned as duplicates are filtered out.
     */
    private List<GamificationCategory> generateWidgetRecommendations(final WidgetSettings settings,
                                                                     final UUID userId,
                                                                     final UUID courseId) {
        int widgetRecCount = settings.getNumberOfRecommendations();
        List<GamificationCategory> recommendations = new ArrayList<>();
        while(widgetRecCount > 0) {
            GamificationCategory recommendation =
                    recommendationCreator.makeRecommendation(userId, courseId, RecommendationType.WIDGET);
            if(!recommendations.contains(recommendation)) {
                recommendations.add(recommendation);
            }
            widgetRecCount--;
        }

        return recommendations;
    }

    private UserWidgetSettingsEntity createDefaultUserWidgetSettings(final UUID userId) {
        UserWidgetSettingsEntity settingsEntity = new UserWidgetSettingsEntity(userId);
        settingsEntity.setNumberOfRecommendations(2);
        settingsEntity.setRecommendationRefreshInterval(12);
        widgetSettingsRepository.save(settingsEntity);
        return settingsEntity;
    }

}
