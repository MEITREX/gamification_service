package de.unistuttgart.iste.meitrex.gamification_service.service.internal;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;

import java.util.UUID;

/**
 * A contract for fetching a user by its {@see UUID}. If no such exists, it is created and initialized. The
 * concrete initialization method must be treated as an implementation detail.
 *
 */
public interface IUserCreator {

    /**
     * Fetches a {@link UserEntity} identified by its {@link UUID} from the underlying persistence mechanism.
     * If no such instance exists, it is created, initialized, persisted, and finally returned.
     *
     * @param userId the {@link UUID} identifying the {@link UserEntity}
     * @return an instance of {@link UserEntity} with the given userId.
     */
    UserEntity fetchOrCreate(UUID userId);

}
