package de.unistuttgart.iste.meitrex.gamification_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties("debug.app.adaptivity")
@Configuration
public class DebugAdaptivityConfiguration {
    private Quests quests = new Quests();

    @Data
    public static class Quests {
        /**
         * Forces daily quest generation to exclusively generate the specified quest type.
         * Must be one of: "EXERCISE", "LEARNING", "SPECIALTY" or null to disable forcing.
         */
        private String forceDailyQuestType = null;
        /**
         * Forces specialty quest generation to exclusively generate the specified quest type.
         * Must be one of: "ALTRUISM", "ASSISTANCE", "CUSTOMIZATION", "IMMERSION", "INCENTIVE", "PROGRESSION",
         * "RISK_REWARD", "SOCIALIZATION", or null to disable forcing.
         */
        private String forceSpecialtyQuestType = null;
    }
}
