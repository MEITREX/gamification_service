package de.unistuttgart.iste.meitrex.gamification_service.keycloak;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@Component
class DefaultUserConfiguration implements IUserConfigurationProvider {


    private static final String GROUP_ATTR_NAME = "gamification_type";


    private static final String GROUP_ATTR_VALUE_NO_GAMIFICATION = "none";

    private static final String GROUP_ATTR_VALUE_GAMIFICATION = "gamification";

    private static final String GROUP_ATTR_VALUE_ADAPTIVE_GAMIFICATION = "adaptive_gamification";


    private static final List<String> GROUP_ATTRB_VALUE_VALUE_LIST = List.of(GROUP_ATTR_VALUE_NO_GAMIFICATION, GROUP_ATTR_VALUE_GAMIFICATION, GROUP_ATTR_VALUE_ADAPTIVE_GAMIFICATION);

    private static final Random RANDOM = new Random();


    private static String getRandomAttributeValue() {
        return GROUP_ATTRB_VALUE_VALUE_LIST.get(RANDOM.nextInt(GROUP_ATTRB_VALUE_VALUE_LIST.size()));
    }


    private final IKeycloakClient keycloakClient;


    public DefaultUserConfiguration(IKeycloakClient keycloakClient) {
        this.keycloakClient = Objects.requireNonNull(keycloakClient);
    }

    @Override
    public boolean isAdaptiveGamificationDisabled(UUID userId) {
        return !fetchAndInitIfEmpty(userId).contains(GROUP_ATTR_VALUE_ADAPTIVE_GAMIFICATION);
    }

    private List<String> fetchAndInitIfEmpty(UUID userId) {
        List<String> attributeList = keycloakClient.getValues(userId, GROUP_ATTR_NAME);
        if(attributeList.isEmpty()) {
            attributeList = List.of(getRandomAttributeValue());
            keycloakClient.setValues(userId, GROUP_ATTR_NAME, attributeList);
        }
        return attributeList;
    }

}
