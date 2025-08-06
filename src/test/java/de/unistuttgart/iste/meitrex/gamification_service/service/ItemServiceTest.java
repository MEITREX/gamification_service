package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.UserInventoryEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.UserRepository;
import de.unistuttgart.iste.meitrex.generated.dto.Inventory;
import de.unistuttgart.iste.meitrex.generated.dto.UserItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.hamcrest.MatcherAssert.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class ItemServiceTest {

    @Autowired
    ItemService itemService;

    @MockitoBean
    private UserRepository userRepository  = mock(UserRepository.class);

    @MockitoBean
    private GoalProgressService goalProgressService = mock(GoalProgressService.class);



    @Test
    void testGetInventoryForUser() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity(userId, new ArrayList<>(), new UserInventoryEntity());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        Inventory inventory = itemService.getInventoryForUser(userId);
        assertThat(inventory.getItems(), hasSize(81));
        assertThat(inventory.getUserId(), is(userId));
        assertThat(inventory.getUnspentPoints(), is(0));
    }

    @Test
    void testGetInventoryForEmptyUser() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity(userId, new ArrayList<>(), new UserInventoryEntity());
        when(goalProgressService.createUser(userId)).thenReturn(user);
        Inventory inventory = itemService.getInventoryForUser(userId);
        assertThat(inventory.getItems(), hasSize(81));
        assertThat(inventory.getUserId(), is(userId));
        assertThat(inventory.getUnspentPoints(), is(0));
    }

    @Test
    void testGetItemsForUser() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity(userId, new ArrayList<>(), new UserInventoryEntity());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        List<UserItem> items = itemService.getItemsForUser(userId);
        assertThat(items, hasSize(81));
    }

    @Test
    void testGetItemsForUserEmptyUser() {
        UUID userId = UUID.randomUUID();
        UserEntity user = new UserEntity(userId, new ArrayList<>(), new UserInventoryEntity());
        when(goalProgressService.createUser(userId)).thenReturn(user);
        List<UserItem> items = itemService.getItemsForUser(userId);
        assertThat(items, hasSize(81));
    }
}
