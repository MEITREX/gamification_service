package de.unistuttgart.iste.meitrex.gamification_service.service;

import java.util.*;

import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.gamification_service.exception.*;

/**
 * A service layer interface for fetching gamification-related user data. The results are meant for external exposure,
 * e.g. via a GraphQL API, and should not be consumed by business logic.
 *
 * @author Philipp Kunz
 */
public interface IUserService {

    /**
     * Fetches for the passed {@code userID} the corresponding instance of {@code User}. If no such entity exists
     * a {@code ResourceNotFoundException} is thrown.
     *
     * @param userID an uuid identifying a user.
     * @return an instance of {@code User} matching the passed user id.
     * @throws ResourceNotFoundException if no matching user entity exists.
     */
    User fetchUser(UUID userID)
        throws ResourceNotFoundException;
}
