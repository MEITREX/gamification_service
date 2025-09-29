package de.unistuttgart.iste.meitrex.gamification_service.service;

import java.util.*;

import de.unistuttgart.iste.meitrex.course_service.client.CourseServiceClient;
import de.unistuttgart.iste.meitrex.gamification_service.exception.ConflictException;
import de.unistuttgart.iste.meitrex.gamification_service.exception.ResourceNotFoundException;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.UserInventoryEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.skilllevels.SkillLevelsEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper.UserMapper;
import de.unistuttgart.iste.meitrex.gamification_service.service.functional.IXPLevelDistance;
import de.unistuttgart.iste.meitrex.gamification_service.service.functional.IXPLevelMapping;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.generated.dto.Leaderboard;
import de.unistuttgart.iste.meitrex.generated.dto.User;
import de.unistuttgart.iste.meitrex.generated.dto.UserInfo;
import de.unistuttgart.iste.meitrex.user_service.client.UserServiceClient;
import de.unistuttgart.iste.meitrex.user_service.exception.UserServiceConnectionException;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.*;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.*;


/**
 * {@link DefaultUserService} implements the logic for handling local user creation on demand.
 *
 * @author Philipp Kunz
 */
@Component
@Transactional
@Slf4j
public class DefaultUserService implements IUserService, IUserCreator {

    private final IXPLevelMapping xpLevelMapping;

    private final IXPLevelDistance xpLevelDistance;

    private final IUserRepository userRepository;

    private final UserMapper userMapper;

    private final int dtoRecursionDepth;

    public DefaultUserService(
            @Autowired IXPLevelMapping xpLevelMapping,
            @Autowired IXPLevelDistance xpLevelDistance,
            @Autowired IUserRepository userRepository,
            @Autowired UserMapper userMapper,
            @Value("${de.unistuttgart.iste.meitrex.gamification_service.service.dtoRecursionDepth:3}")
            int dtoRecursionDepth
    ) {
        this.xpLevelMapping = Objects.requireNonNull(xpLevelMapping);
        this.xpLevelDistance = Objects.requireNonNull(xpLevelDistance);
        this.userRepository = Objects.requireNonNull(userRepository);
        this.userMapper = Objects.requireNonNull(userMapper);
        this.dtoRecursionDepth = dtoRecursionDepth;
    }


    @Override
    public UserEntity fetchOrCreate(UUID userId) {
        final UserEntity user =  this.userRepository
                .findById(userId)
                .orElseGet(()  -> this.userRepository.save(new UserEntity(userId, 0, null, null, null, null, new ArrayList<>(), null, new ArrayList<>(), null, new ArrayList<>())));
        return user;
    }

    @Override
    public User fetchUser(UUID userID)
            throws ResourceNotFoundException {
        final UserEntity user = this.fetchOrThrow(userID);
        this.augmentExceedingXP(user);
        this.augmentRequiredXP(user);
        this.augmentLevel(user);
        return this.userMapper.toDTO(user, this.dtoRecursionDepth);
    }

    private void augmentRequiredXP(UserEntity user)
            throws ResourceNotFoundException {
        final double curXp = user.getXpValue();
        final int curLevel = this.xpLevelMapping.calcLevel(curXp);
        final double requiredXP =  this.xpLevelDistance.calcDistance(curXp, curLevel + 1);
        user.setRequiredXP(requiredXP);
    }

    private void augmentExceedingXP(UserEntity user)
            throws ResourceNotFoundException {
        final double curXp = user.getXpValue();
        final int curLevel = this.xpLevelMapping.calcLevel(curXp);
        final double exceedingXP = -1 * this.xpLevelDistance.calcDistance(curXp, curLevel);
        user.setExceedingXP(exceedingXP);
    }

    private void augmentLevel(UserEntity user) {
        int level =  this.xpLevelMapping.calcLevel(user.getXpValue());
        user.setLevel(level);
    }

    private UserEntity fetchOrThrow(UUID userID) {
        return this.userRepository.findById(userID)
                .orElseThrow(ResourceNotFoundException::new);
    }
}
