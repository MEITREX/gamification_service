package de.unistuttgart.iste.meitrex.gamification_service.keycloak;

import de.unistuttgart.iste.meitrex.gamification_service.model.PatternTheme;

import java.util.*;

interface IKeycloakClient {

    List<String> getValues(UUID userId, String attrName);

    void setValues(UUID userId, String attrName, List<String> values);
}
