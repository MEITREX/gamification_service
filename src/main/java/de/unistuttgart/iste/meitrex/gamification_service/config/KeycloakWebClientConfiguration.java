package de.unistuttgart.iste.meitrex.gamification_service.config;

import java.util.*;

import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.web.reactive.function.client.*;

@Configuration
public class KeycloakWebClientConfiguration {

    private String keycloakUrl;

    public KeycloakWebClientConfiguration(@Value("${keycloak.url:http://keylcoak:8080}") String keycloakUrl) {
        this.keycloakUrl = Objects.requireNonNull(keycloakUrl);
    }

    @Bean
    public WebClient keycloakServiceClient() {
        return WebClient
                .builder()
                .baseUrl(this.keycloakUrl)
                .build();
    }

}
