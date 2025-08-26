package de.unistuttgart.iste.meitrex.gamification_service.config;

import de.unistuttgart.iste.meitrex.common.ollama.OllamaClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OllamaClientConfiguration {

    @Value("${ollama.url}")
    private String ollamaUrl;

    @Bean
    public OllamaClient ollamaClient() {
        return new OllamaClient(ollamaUrl);
    }
}
