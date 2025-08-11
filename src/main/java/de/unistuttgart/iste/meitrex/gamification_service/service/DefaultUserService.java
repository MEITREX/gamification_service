package de.unistuttgart.iste.meitrex.gamification_service.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;


/**
 * {@link DefaultUserService} implements the logic for handling local user creation on demand.
 * Consider extracting an interface if user-related logic should be exposed via this service's interface,
 * e.g., its GraphQL layer.
 *
 * @author Philipp Kunz
 */
@Component
class DefaultUserService {

    private final IUserRepository userRepository;


    public DefaultUserService(@Autowired IUserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    /**
     * Fetches a {@link UserEntity} identified by its {@link UUID} from the underlying persistence mechanism.
     * If no such instance exists, it is created, initialized, persisted, and finally returned.
     *
     * @param userId the {@link UUID} identifying the {@link UserEntity}
     * @return an instance of {@link UserEntity} with the given userId.
     */
    UserEntity fetchOrCreate(UUID userId) {
        return this.userRepository
                .findById(userId)
                .orElseGet(()  -> this.userRepository.save(new UserEntity(userId, new ArrayList<>())));
    }
}
