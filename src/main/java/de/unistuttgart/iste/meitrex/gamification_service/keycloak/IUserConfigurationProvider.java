package de.unistuttgart.iste.meitrex.gamification_service.keycloak;

import java.util.UUID;

public interface IUserConfigurationProvider {

    boolean isAdaptiveGamificationDisabled(UUID userId);

}
