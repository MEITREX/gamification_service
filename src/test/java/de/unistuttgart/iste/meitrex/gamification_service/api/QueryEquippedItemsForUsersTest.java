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
import de.unistuttgart.iste.meitrex.generated.dto.EquippedItems;
import de.unistuttgart.iste.meitrex.generated.dto.Inventory;
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
public class QueryEquippedItemsForUsersTest {

    UUID courseId = UUID.randomUUID();

    @Autowired
    DefaultItemService itemService;

    @InjectCurrentUserHeader
    private final LoggedInUser loggedInUser = userWithMembershipInCourseWithId(courseId, LoggedInUser.UserRoleInCourse.STUDENT);
    @Autowired
    private IUserRepository userRepository;


    @Test
    void testInventoryForUser(final GraphQlTester tester) {
        UUID itemId = UUID.fromString(ItemUtil.PATTERN_THEME_ID);
        UUID itemId2 = UUID.fromString(ItemUtil.COLOR_THEME_ID);
        UUID itemId3 = UUID.fromString(ItemUtil.DEFAULT_TUTOR_ITEM_ID);
        UserEntity user = new UserEntity(loggedInUser.getId(), 0, null, null, null, null, new ArrayList<>(), new UserInventoryEntity(), new ArrayList<>(), null, new ArrayList<>());
        ItemInstanceEntity itemInstanceEntity = new ItemInstanceEntity();
        itemInstanceEntity.setPrototypeId(itemId);
        itemInstanceEntity.setEquipped(false);
        itemInstanceEntity.setUniqueDescription("");
        itemInstanceEntity.setItemType(ItemType.PatternTheme);
        ItemInstanceEntity itemInstanceEntity2 = new ItemInstanceEntity();
        itemInstanceEntity2.setPrototypeId(itemId2);
        itemInstanceEntity2.setEquipped(true);
        itemInstanceEntity2.setUniqueDescription("");
        itemInstanceEntity2.setItemType(ItemType.ColorTheme);
        ItemInstanceEntity itemInstanceEntity3 = new ItemInstanceEntity();
        itemInstanceEntity3.setPrototypeId(itemId3);
        itemInstanceEntity3.setEquipped(true);
        itemInstanceEntity3.setUniqueDescription("");
        itemInstanceEntity3.setItemType(ItemType.Tutor);
        user.getInventory().getItems().add(itemInstanceEntity);
        user.getInventory().getItems().add(itemInstanceEntity2);
        user.getInventory().getItems().add(itemInstanceEntity3);
        user.getInventory().setUnspentPoints(0);
        userRepository.save(user);

        final String query = """
                query {
                    equippedItemsForUsers(userIds: ["%s"])
                    {
                        userId
                        items
                        {
                            id
                            uniqueDescription
                            equipped
                            unlocked
                        }
                    }
                }
                """.formatted(loggedInUser.getId());

        List<EquippedItems> equippedItemsList = tester.document(query)
                .execute()
                .path("equippedItemsForUsers").entityList(EquippedItems.class).get();

        assertThat(equippedItemsList, hasSize(1));
        EquippedItems equippedItems = equippedItemsList.get(0);
        System.out.println(equippedItems.toString());
        assertThat(equippedItems.getItems(), hasSize(2));
        assertThat(equippedItems.getItems().stream().anyMatch(item -> item.getId().equals(UUID.fromString(ItemUtil.DEFAULT_TUTOR_ITEM_ID))), is(true));
    }
}
