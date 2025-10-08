package de.unistuttgart.iste.meitrex.gamification_service.test_util;

import de.unistuttgart.iste.meitrex.gamification_service.keycloak.DefaultKeycloakClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class MockKeycloakClientConfig {

    @Bean
    public DefaultKeycloakClient defaultKeycloakClient() {
        return Mockito.mock(DefaultKeycloakClient.class);
    }
}
