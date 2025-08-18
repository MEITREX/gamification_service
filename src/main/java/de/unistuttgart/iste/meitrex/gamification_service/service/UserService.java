package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.UserInventoryEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Optional<UserEntity> getUser(final UUID userId) {
        return userRepository.findById(userId);
    }

    public UserEntity getOrCreateUser(final UUID userId) {
        return userRepository.findById(userId).orElseGet(() -> createUser(userId));
    }

    public UserEntity createUser(final UUID userId) {
        UserEntity userEntity = new UserEntity(userId, new ArrayList<>(), new UserInventoryEntity(), new ArrayList<>());
        userEntity = userRepository.save(userEntity);
        log.info("Created user with id {}", userId);
        log.info("Created user {}", userEntity);
        return userEntity;
    }

    public UserEntity upsertUser(final UserEntity userEntity) {
        return userRepository.save(userEntity);
    }
}
