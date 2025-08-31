package de.unistuttgart.iste.meitrex.gamification_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties("app.adaptivity")
@Configuration
public class AdaptivityConfiguration {
    private int maxAdaptiveAchievementCount;
    private float skillLevelQuestMinBloomScore;
    private float exerciseQuestRandomPickProbability;
    private float learningQuestRandomPickProbability;
    private int questBaseRewardPoints;
    private String immersiveTutorOllamaModel;
    private int immersiveTutorSpeechRefreshIntervalMinutes;
    private String immersiveTutorSpeechGeneric;
    private int widgetRecommendationDefaultFeedbackRequestIntervalDays;
    private int widgetRecommendationMinFeedbackRequestIntervalDays;
    private int widgetRecommendationMaxFeedbackRequestIntervalDays;
}
