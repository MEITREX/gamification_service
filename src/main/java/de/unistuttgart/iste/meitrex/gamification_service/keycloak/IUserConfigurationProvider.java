package de.unistuttgart.iste.meitrex.gamification_service.keycloak;

import java.util.UUID;

/**
 * Provides access to user-specific configuration settings related to gamification behavior. Implementations of this
 * interface determine whether certain gamification mode are enabled or disabled for a given user.
 *
 * @author Philipp Kunz
 */
public interface IUserConfigurationProvider {

    /**
     * Checks whether adaptive gamification is disabled for the specified user.
     *
     * @param userId the unique identifier of the user in Keycloak.
     * @return {@code true} if adaptive gamification is disabled for this user, {@code false} otherwise.
     */
    boolean isAdaptiveGamificationDisabled(UUID userId);

}
