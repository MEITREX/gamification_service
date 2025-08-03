package de.unistuttgart.iste.meitrex.gamification_service.api;

import de.unistuttgart.iste.meitrex.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.meitrex.common.testutil.InjectCurrentUserHeader;
import de.unistuttgart.iste.meitrex.common.testutil.MockTestPublisherConfiguration;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemInstanceEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemType;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.UserInventoryEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.UserRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.ItemService;
import de.unistuttgart.iste.meitrex.gamification_service.test_config.MockContentServiceClientConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.test_config.MockCourseServiceClientConfiguration;
import de.unistuttgart.iste.meitrex.generated.dto.Inventory;
import de.unistuttgart.iste.meitrex.generated.dto.UserItem;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static de.unistuttgart.iste.meitrex.common.testutil.TestUsers.userWithMembershipInCourseWithId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ContextConfiguration(classes = {MockTestPublisherConfiguration.class, MockContentServiceClientConfiguration.class, MockCourseServiceClientConfiguration.class})
@GraphQlApiTest
@Transactional
@ActiveProfiles("test")
public class MutationItemRewardTest {
    UUID courseId = UUID.randomUUID();

    @Autowired
    ItemService itemService;

    @InjectCurrentUserHeader
    private final LoggedInUser loggedInUser = userWithMembershipInCourseWithId(courseId, LoggedInUser.UserRoleInCourse.STUDENT);
    @Autowired
    private UserRepository userRepository;

    @Test
    void testItemRewardCompensationItem(final GraphQlTester tester) {
        UUID itemId = UUID.fromString("6fb8b726-2db2-4992-9e63-f3aa57aa4520");
        UserEntity user = new UserEntity(loggedInUser.getId(), new ArrayList<>(), new ArrayList<>(), new UserInventoryEntity());
        ItemInstanceEntity itemInstanceEntity = new ItemInstanceEntity();
        itemInstanceEntity.setPrototypeId(itemId);
        itemInstanceEntity.setEquipped(false);
        itemInstanceEntity.setUniqueDescription("");
        itemInstanceEntity.setItemType(ItemType.PatternTheme);
        user.getInventory().getItems().add(itemInstanceEntity);
        user.getInventory().setUnspentPoints(0);
        userRepository.save(user);

        final String query = """
                mutation {
                    itemReward(itemId: "%s")
                    {
                        userId
                        unspentPoints
                        items
                        {
                            id
                            uniqueDescription
                            equipped
                            unlocked
                        }
                    }
                }
                """.formatted(itemId);

        Inventory inventory = tester.document(query)
                .execute()
                .path("itemReward").entity(Inventory.class).get();
        assertThat(inventory.getUnspentPoints(), is(2500));
    }

    @Test
    void testItemRewardItem(final GraphQlTester tester) {
        UUID itemId = UUID.fromString("6fb8b726-2db2-4992-9e63-f3aa57aa4520");
        UserEntity user = new UserEntity(loggedInUser.getId(), new ArrayList<>(), new ArrayList<>(), new UserInventoryEntity());
        user.getInventory().setUnspentPoints(0);
        userRepository.save(user);

        final String query = """
                mutation {
                    itemReward(itemId: "%s")
                    {
                        userId
                        unspentPoints
                        items
                        {
                            id
                            uniqueDescription
                            equipped
                            unlocked
                            unlockedTime
                        }
                    }
                }
                """.formatted(itemId);

        Inventory inventory = tester.document(query)
                .execute()
                .path("itemReward").entity(Inventory.class).get();
        assertThat(inventory.getUnspentPoints(), is(0));
        UserItem unlockedItem = inventory.getItems().stream().filter(userItem -> userItem.getId().equals(itemId)).findFirst().get();
        assertThat(unlockedItem.getUnlocked(), is(true));
        assertThat(unlockedItem.getUnlockedTime().toLocalDate().getDayOfMonth(), is(OffsetDateTime.now().getDayOfMonth()));
    }
}
