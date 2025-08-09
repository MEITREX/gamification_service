package de.unistuttgart.iste.meitrex.gamification_service.service.recommendation;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation.UserWidgetRecommendationsEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation.UserWidgetSettingsEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.recommendation.UserWidgetRecommendationsRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.recommendation.UserWidgetSettingsRepository;
import de.unistuttgart.iste.meitrex.gamification_service.recommendation.RecommendationType;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategoryRecommendation;
import de.unistuttgart.iste.meitrex.generated.dto.WidgetSettings;
import de.unistuttgart.iste.meitrex.generated.dto.WidgetSettingsInput;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WidgetRecommendationService {
    private final RecommendationService recommendationService;
    private final UserWidgetRecommendationsRepository widgetRecommendationsRepository;
    private final UserWidgetSettingsRepository widgetSettingsRepository;
    private final ModelMapper modelMapper;

    public List<GamificationCategoryRecommendation> getUserWidgetRecommendations(@NotNull final UUID userId,
                                                                                 @NotNull final UUID courseId) {
        WidgetSettings settings = getUserWidgetSettings(userId);

        if(settings.getNumberOfRecommendations() == 0)
            return List.of();

        Optional<UserWidgetRecommendationsEntity> recommendationsEntity =
                widgetRecommendationsRepository.findById(userId);

        // check if we already have some recommendations for the user which aren't too old, otherwise generate new ones
        if(recommendationsEntity.isEmpty() || isNewWidgetRecommendationDue(recommendationsEntity.get(), settings)) {
            if (recommendationsEntity.isEmpty())
                recommendationsEntity = Optional.of(new UserWidgetRecommendationsEntity(userId));

            var recommendations = IntStream.range(0, settings.getNumberOfRecommendations())
                    .mapToObj(i -> recommendationService.makeRecommendation(userId, courseId, RecommendationType.WIDGET))
                    .toList();
            recommendationsEntity.get().setRecommendations(recommendations);

            // calculate generation time
            // we actually do not want to save the current time, but the last time of the interval which is
            // a division of 24 hours. E.g. if it's currently 14:30, and our interval is 6 hours, we want to save
            // the time 12:00, so that we always generate new recommendations
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
                .map(x -> new GamificationCategoryRecommendation(
                        x,
                        recommendationService.isFeedbackRequestDue(userId, x)))
                .toList();
    }

    public WidgetSettings setUserWidgetSettings(final UUID userId, final WidgetSettingsInput input) {
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

    public WidgetSettings getUserWidgetSettings(final UUID userId) {
        Optional<UserWidgetSettingsEntity> settingsEntity = widgetSettingsRepository.findById(userId);

        if(settingsEntity.isEmpty()) {
            settingsEntity = Optional.of(createDefaultUserWidgetSettings(userId));
        }

        return modelMapper.map(settingsEntity.get(), WidgetSettings.class);
    }

    private UserWidgetSettingsEntity createDefaultUserWidgetSettings(final UUID userId) {
        UserWidgetSettingsEntity settingsEntity = new UserWidgetSettingsEntity(userId);

        settingsEntity.setNumberOfRecommendations(2);
        settingsEntity.setRecommendationRefreshInterval(12);

        widgetSettingsRepository.save(settingsEntity);
        return settingsEntity;
    }

    private boolean isNewWidgetRecommendationDue(final UserWidgetRecommendationsEntity entity,
                                                 final WidgetSettings settings) {
        LocalDateTime previousTime = entity.getGenerationTime();

        LocalDateTime nextGenerationDue = LocalDate.now().atStartOfDay();
        while(nextGenerationDue.isBefore(previousTime) ||
              nextGenerationDue.isEqual(previousTime)) {
            nextGenerationDue = nextGenerationDue.plusHours(settings.getRecommendationRefreshInterval());
        }

        return nextGenerationDue.isBefore(LocalDateTime.now());
    }
}
