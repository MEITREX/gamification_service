package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.exception.ResourceNotFoundException;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper.UserMapper;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.functional.DefaultXPImplementation;
import de.unistuttgart.iste.meitrex.gamification_service.service.functional.IXPLevelDistance;
import de.unistuttgart.iste.meitrex.gamification_service.service.functional.IXPLevelMapping;
import de.unistuttgart.iste.meitrex.generated.dto.Achievement;
import de.unistuttgart.iste.meitrex.generated.dto.User;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;


class UserServiceTest {



    private UserMapper userMapper = new UserMapper();

    private IXPLevelDistance xpLevelDistance = new DefaultXPImplementation(40, 600.0);

    private IXPLevelMapping xpLevelMapping = new DefaultXPImplementation(40, 600.0);


    @Test
    void testFetchUserThrowsWhenNotFound() {
        final UUID id =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final IUserRepository userRepository = mock(IUserRepository.class);
        when(userRepository.findById(id)).thenReturn(Optional.empty());
        final DefaultUserService userService =
                new DefaultUserService(xpLevelMapping, xpLevelDistance, userRepository, userMapper, 3);
        assertThrows(ResourceNotFoundException.class, () -> userService.fetchUser(id));
    }


    @Test
    void testFetchOrCreateCreatesNewUserWhenMissing() {
        //Populating Repository
        final UUID newUserId =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final IUserRepository userRepository = mock(IUserRepository.class);
        when(userRepository.findById(newUserId))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        //UserService instantiation
        final DefaultUserService userService =
                new DefaultUserService(xpLevelMapping, xpLevelDistance, userRepository, userMapper, 3);
        // Execution and verification
        UserEntity created = userService.fetchOrCreate(newUserId);
        assertEquals(newUserId, created.getId());
        verify(userRepository).save(any(UserEntity.class));
    }

    // Required XP Augmentation Testing

    @Test
    void testRequiredXPAugmentationForNewUser() {
        // Populating Repository
        final UUID newUserID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UserEntity newUserEntity =  UserEntity
                .builder()
                .id(newUserID)
                .xpValue(0)
                .userName("user")
                .build();
        final DefaultUserService userService = createUserService(newUserEntity);
        final User newUser = userService.fetchUser(newUserID);
        assertEquals(105.65475543340874, newUser.getRequiredXP(), 0.1);
    }

    @Test
    void testRequiredXPAugmentationForLevelZeroUser() {
        // Populating Repository
        final UUID newUserID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UserEntity newUserEntity =  UserEntity
                .builder()
                .id(newUserID)
                .xpValue(10)
                .userName("user")
                .build();
        final DefaultUserService userService = createUserService(newUserEntity);
        final User newUser = userService.fetchUser(newUserID);
        assertEquals(95.65475543340874, newUser.getRequiredXP(), 0.1);
    }

    @Test
    void testRequiredXPAugmentationForLevelOneUser() {
        // Populating Repository
        final UUID newUserID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UserEntity newUserEntity =  UserEntity
                .builder()
                .id(newUserID)
                .xpValue(106)
                .userName("user")
                .build();
        final DefaultUserService userService = createUserService(newUserEntity);
        final User newUser = userService.fetchUser(newUserID);
        assertEquals(180.27275283179745, newUser.getRequiredXP(), 0.1);
    }


    // Exceeding XP Augmentation Testing

    @Test
    void testExceedingXPAugmentationForNewUser() {
        // Populating Repository
        final UUID newUserID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UserEntity newUserEntity =  UserEntity
                .builder()
                .id(newUserID)
                .xpValue(0)
                .userName("user")
                .build();
        final DefaultUserService userService = createUserService(newUserEntity);
        final User newUser = userService.fetchUser(newUserID);
        assertEquals(0, newUser.getExceedingXP()*newUser.getExceedingXP(), 0.1);
    }

    @Test
    void testExceedingXPAugmentationForLevelZeroUser() {
        // Populating Repository
        final UUID newUserID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UserEntity newUserEntity =  UserEntity
                .builder()
                .id(newUserID)
                .xpValue(10)
                .userName("user")
                .build();
        final DefaultUserService userService = createUserService(newUserEntity);
        final User newUser = userService.fetchUser(newUserID);
        assertEquals(10, newUser.getExceedingXP(), 0.1);
    }

    @Test
    void testExceedingXPAugmentationForLevelOneUser() {
        // Populating Repository
        final UUID newUserID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UserEntity newUserEntity =  UserEntity
                .builder()
                .id(newUserID)
                .xpValue(150)
                .userName("user")
                .build();
        final DefaultUserService userService = createUserService(newUserEntity);
        final User newUser = userService.fetchUser(newUserID);
        assertEquals(44.34524456659126, newUser.getExceedingXP(), 0.1);
    }

    // Level Augmentation Testing

    @Test
    void testLevelAugmentationForNewUser() {
        // Populating Repository
        final UUID newUserID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UserEntity newUserEntity =  UserEntity
                .builder()
                .id(newUserID)
                .xpValue(0)
                .userName("user")
                .build();
        final DefaultUserService userService = createUserService(newUserEntity);
        final User newUser = userService.fetchUser(newUserID);
        assertEquals(0, newUser.getLevel());
    }

    @Test
    void testLevelAugmentationForLevelZeroUser() {
        // Populating Repository
        final UUID newUserID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UserEntity newUserEntity =  UserEntity
                .builder()
                .id(newUserID)
                .xpValue(70)
                .userName("user")
                .build();
        final DefaultUserService userService = createUserService(newUserEntity);
        final User newUser = userService.fetchUser(newUserID);
        assertEquals(0, newUser.getLevel());
    }

    @Test
    void testLevelAugmentationForLevelOneUser() {
        // Populating Repository
        final UUID newUserID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UserEntity newUserEntity =  UserEntity
                .builder()
                .id(newUserID)
                .xpValue(170)
                .userName("user")
                .build();
        final DefaultUserService userService = createUserService(newUserEntity);
        final User newUser = userService.fetchUser(newUserID);
        assertEquals(1, newUser.getLevel());
    }

    @Test
    void testLevelAugmentationForLExperiencedUser() {
        // Populating Repository
        final UUID newUserID =  UUID.fromString("00000000-0000-0000-0000-000000000000");
        final UserEntity newUserEntity =  UserEntity
                .builder()
                .id(newUserID)
                .xpValue(10400)
                .userName("user")
                .build();
        final DefaultUserService userService = createUserService(newUserEntity);
        final User newUser = userService.fetchUser(newUserID);
        assertEquals(22, newUser.getLevel());
    }

    private DefaultUserService createUserService(UserEntity userEntity) {
        final IUserRepository userRepository = mock(IUserRepository.class);
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));
        return new DefaultUserService(xpLevelMapping, xpLevelDistance, userRepository, userMapper, 3);
    }

}

