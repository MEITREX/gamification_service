package de.unistuttgart.iste.meitrex.gamification_service.config;

import de.unistuttgart.iste.meitrex.common.dapr.TopicPublisher;
import io.dapr.client.DaprClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
public class TopicPublisherConfiguration {
    @Bean
    public TopicPublisher topicPublisher() {
        return new TopicPublisher(new DaprClientBuilder().build());
    }
}
