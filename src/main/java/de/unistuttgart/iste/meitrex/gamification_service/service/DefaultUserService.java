package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ICourseRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

/**
 * Bundles logic for handling instances of {@link UserEntity}. Consider extracting an interface, once respective
 * business logic shall be exposed for external usage.
 *
 * @author Philipp Kunz
 *
 */
@Component
class DefaultUserService {

    private final IUserRepository userRepository;


    public DefaultUserService(
            @Autowired IUserRepository userRepository
    ) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    /**
     * Fetches an instance of {@link UserEntity} featuring the passed courseId. If no such entity exists, it is created.
     *
     * @param userId instance of {@link UUID} identifying an instance of {@link UserEntity}.
     * @return an instance of {@link UserEntity} featuring the passed userId.
     */
    UserEntity fetchOrCreate(UUID userId) {

        return this.userRepository
                .findById(userId)
                .orElseGet(()  -> this.userRepository.save(new UserEntity(userId, new ArrayList<>())));
    }
}
