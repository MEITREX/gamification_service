package de.unistuttgart.iste.meitrex.gamification_service.service;

import java.util.*;

import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.gamification_service.exception.*;

public interface IUserService {

    User fetchUser(UUID userID)
        throws ResourceNotFoundException;
}
