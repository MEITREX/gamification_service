package de.unistuttgart.iste.meitrex.gamification_service.keycloak;

import de.unistuttgart.iste.meitrex.gamification_service.model.PatternTheme;

import java.util.*;

/**
 * Defines operations for interacting with a Keycloak server to manage user attributes. Implementations of this interface
 * provide methods to read and modify custom user attributes stored in Keycloak’s user profile data. Attributes are
 * identified by their names and can hold multiple string values.
 *
 * @author Philipp Kunz
 */
interface IKeycloakClient {


    /**
     * Retrieves the list of values associated with a specific user attribute.
     *
     * @param userId the unique identifier of the user in Keycloak.
     * @param attrName the name of the attribute to retrieve.
     * @return an immutable list of attribute values, or an empty list if the attribute does not exist.
     */
    List<String> getValues(UUID userId, String attrName);


    /**
     * Sets or updates the list of values for a specific user attribute. If {@code values} is {@code null} or empty,
     * the attribute will be removed from the user's profile. Otherwise, the attribute will be created or updated with
     * the given values.
     *
     * @param userId   the unique identifier of the user in Keycloak.
     * @param attrName the name of the attribute to modify.
     * @param values   the list of values to assign to the attribute, or {@code null}/empty to remove it.
     */
    void setValues(UUID userId, String attrName, List<String> values);
}
