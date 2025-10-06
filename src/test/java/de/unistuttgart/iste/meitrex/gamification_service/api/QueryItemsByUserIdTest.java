package de.unistuttgart.iste.meitrex.gamification_service.api;

import de.unistuttgart.iste.meitrex.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.meitrex.common.testutil.InjectCurrentUserHeader;
import de.unistuttgart.iste.meitrex.common.testutil.MockTestPublisherConfiguration;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemInstanceEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemType;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.UserInventoryEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.DefaultItemService;
import de.unistuttgart.iste.meitrex.gamification_service.test_config.MockContentServiceClientConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.test_config.MockCourseServiceClientConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.test_util.ItemUtil;
import de.unistuttgart.iste.meitrex.generated.dto.UserItem;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static de.unistuttgart.iste.meitrex.common.testutil.TestUsers.userWithMembershipInCourseWithId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

@ContextConfiguration(classes = {MockTestPublisherConfiguration.class, MockContentServiceClientConfiguration.class, MockCourseServiceClientConfiguration.class})
@GraphQlApiTest
@Transactional
@ActiveProfiles("test")
public class QueryItemsByUserIdTest {
    UUID courseId = UUID.randomUUID();

    @Autowired
    DefaultItemService itemService;

    @InjectCurrentUserHeader
    private final LoggedInUser loggedInUser = userWithMembershipInCourseWithId(courseId, LoggedInUser.UserRoleInCourse.STUDENT);
    @Autowired
    private IUserRepository userRepository;

    @Test
    void testQueryItemsByUserId(final GraphQlTester tester) {
        UUID itemId = UUID.fromString(ItemUtil.DEFAULT_TUTOR_ITEM_ID);
        UserEntity user = new UserEntity(loggedInUser.getId(), 0, null, null, null, null, new ArrayList<>(), new UserInventoryEntity(), new ArrayList<>(), null, new ArrayList<>());
        ItemInstanceEntity itemInstanceEntity = new ItemInstanceEntity();
        itemInstanceEntity.setPrototypeId(itemId);
        itemInstanceEntity.setEquipped(false);
        itemInstanceEntity.setUniqueDescription("");
        itemInstanceEntity.setItemType(ItemType.PatternTheme);
        user.getInventory().getItems().add(itemInstanceEntity);
        user.getInventory().setUnspentPoints(0);
        userRepository.save(user);

        final String query = """
                query {
                    itemsByUserId(userId: "%s")
                    {
                        id
                        uniqueDescription
                        equipped
                        unlocked
                    }
                }
                """.formatted(loggedInUser.getId());

        List<UserItem> userItemList = tester.document(query)
                .execute()
                .path("itemsByUserId").entityList(UserItem.class).get();
        UserItem unlockedItem = userItemList.stream().filter(userItem -> userItem.getId().equals(itemId)).findFirst().get();
        assertThat(unlockedItem.getUnlocked(), is(true));
        assertThat(userItemList, hasSize(81));
    }

    @Test
    void testQueryItemByUserIdUserNotExists(final GraphQlTester tester) {
        final String query = """
                query {
                    itemsByUserId(userId: "%s")
                    {
                        id
                        uniqueDescription
                        equipped
                        unlocked
                    }
                }
                """.formatted(loggedInUser.getId());

        UUID itemId = UUID.fromString(ItemUtil.DEFAULT_TUTOR_ITEM_ID);

        List<UserItem> userItemList = tester.document(query)
                .execute()
                .path("itemsByUserId").entityList(UserItem.class).get();
        UserItem unlockedItem = userItemList.stream().filter(userItem -> userItem.getId().equals(itemId)).findFirst().get();
        assertThat(unlockedItem.getUnlocked(), is(true));
        assertThat(userItemList, hasSize(81));
    }
}
