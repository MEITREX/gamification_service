package de.unistuttgart.iste.meitrex.gamification_service.api;

import de.unistuttgart.iste.meitrex.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.meitrex.common.testutil.InjectCurrentUserHeader;
import de.unistuttgart.iste.meitrex.common.testutil.MockTestPublisherConfiguration;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.UserInventoryEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.DefaultItemService;
import de.unistuttgart.iste.meitrex.gamification_service.test_config.MockContentServiceClientConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.test_config.MockCourseServiceClientConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.test_util.ItemUtil;
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
import static org.hamcrest.Matchers.nullValue;

@ContextConfiguration(classes = {MockTestPublisherConfiguration.class, MockContentServiceClientConfiguration.class, MockCourseServiceClientConfiguration.class})
@GraphQlApiTest
@Transactional
@ActiveProfiles("test")
public class MutationBuyItemTest {
    UUID courseId = UUID.randomUUID();

    @Autowired
    DefaultItemService itemService;

    @InjectCurrentUserHeader
    private final LoggedInUser loggedInUser = userWithMembershipInCourseWithId(courseId, LoggedInUser.UserRoleInCourse.STUDENT);
    @Autowired
    private IUserRepository userRepository;

    @Test
    void testBuyItem(final GraphQlTester tester) {
        UUID itemId = UUID.fromString(ItemUtil.NEW_ITEM_ID);
        UserEntity user = new UserEntity(loggedInUser.getId(), 0, null, null, null, null, new ArrayList<>(), new UserInventoryEntity(), new ArrayList<>(), null, new ArrayList<>());

        user.getInventory().setUnspentPoints(10000);
        userRepository.save(user);

        final String query = """
                mutation {
                    buyItem(itemId: "%s")
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
                .path("buyItem").entity(Inventory.class).get();
        UserItem userItem = inventory.getItems().stream().filter(userItem1 -> userItem1.getId().equals(itemId)).findFirst().get();
        assertThat(userItem.getUnlockedTime().toLocalDate().getDayOfMonth(), is(OffsetDateTime.now().getDayOfMonth()));
        assertThat(userItem.getUnlocked(), is(true));
        assertThat(inventory.getUnspentPoints(), is(5000));
    }

    @Test
    void testBuyItemNoMoney(final GraphQlTester tester) {
        UUID itemId = UUID.fromString(ItemUtil.NEW_ITEM_ID);
        UserEntity user = new UserEntity(loggedInUser.getId(), 0, null, null, null, null, new ArrayList<>(), new UserInventoryEntity(), new ArrayList<>(), null, new ArrayList<>());
        user.getInventory().setUnspentPoints(1000);
        userRepository.save(user);

        final String query = """
                mutation {
                    buyItem(itemId: "%s")
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
                .path("buyItem").entity(Inventory.class).get();
        UserItem userItem = inventory.getItems().stream().filter(userItem1 -> userItem1.getId().equals(itemId)).findFirst().get();
        assertThat(userItem.getUnlockedTime(), nullValue());
        assertThat(userItem.getUnlocked(), is(false));
        assertThat(inventory.getUnspentPoints(), is(1000));
    }
}
