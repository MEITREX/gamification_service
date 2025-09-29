package de.unistuttgart.iste.meitrex.gamification_service.keycloak;

import de.unistuttgart.iste.meitrex.gamification_service.model.PatternTheme;

import java.util.*;

public interface IKeycloakClient {

    List<String> getValues(UUID userId, String attrName);

}
