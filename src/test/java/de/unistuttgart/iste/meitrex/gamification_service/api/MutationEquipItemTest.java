package de.unistuttgart.iste.meitrex.gamification_service.api;

import de.unistuttgart.iste.meitrex.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.meitrex.common.testutil.InjectCurrentUserHeader;
import de.unistuttgart.iste.meitrex.common.testutil.MockTestPublisherConfiguration;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.course_service.client.CourseServiceClient;
import de.unistuttgart.iste.meitrex.gamification_service.model.ItemData;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemInstanceEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemType;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.UserInventoryEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.DefaultItemService;
import de.unistuttgart.iste.meitrex.gamification_service.test_config.MockContentServiceClientConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.test_config.MockCourseServiceClientConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.test_util.ItemUtil;
import de.unistuttgart.iste.meitrex.gamification_service.test_util.MockKeycloakClientConfig;
import de.unistuttgart.iste.meitrex.generated.dto.Chapter;
import de.unistuttgart.iste.meitrex.generated.dto.Inventory;
import de.unistuttgart.iste.meitrex.generated.dto.Item;
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
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {MockTestPublisherConfiguration.class, MockContentServiceClientConfiguration.class, MockCourseServiceClientConfiguration.class, MockKeycloakClientConfig.class})
@GraphQlApiTest
@Transactional
@ActiveProfiles("test")
public class MutationEquipItemTest {
    UUID courseId = UUID.randomUUID();

    @Autowired
    DefaultItemService itemService;

    @InjectCurrentUserHeader
    private final LoggedInUser loggedInUser = userWithMembershipInCourseWithId(courseId, LoggedInUser.UserRoleInCourse.STUDENT);
    @Autowired
    private IUserRepository userRepository;

    @Test
    void testEquipItem(final GraphQlTester tester) {
        UUID itemId = UUID.fromString(ItemUtil.TUTOR_ITEM_ID);
        UUID defaultItemId = UUID.fromString(ItemUtil.DEFAULT_TUTOR_ITEM_ID);
        UserEntity user = new UserEntity(loggedInUser.getId(), 0, null, null, null, null, new ArrayList<>(), new UserInventoryEntity(), new ArrayList<>(), null, new ArrayList<>());
        ItemInstanceEntity itemInstanceEntity = new ItemInstanceEntity();
        itemInstanceEntity.setPrototypeId(itemId);
        itemInstanceEntity.setEquipped(false);
        itemInstanceEntity.setUniqueDescription("");
        itemInstanceEntity.setItemType(ItemType.Tutor);
        user.getInventory().getItems().add(itemInstanceEntity);
        userRepository.save(user);

        final String query = """
                mutation {
                    equipItem(itemId: "%s")
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
                .path("equipItem").entity(Inventory.class).get();
        UserItem equipedItem = inventory.getItems().stream().filter(userItem -> userItem.getId().equals(itemId)).findFirst().get();
        UserItem unequipedItem = inventory.getItems().stream().filter(userItem -> userItem.getId().equals(defaultItemId)).findFirst().get();
        assertThat(equipedItem.getEquipped(), is(true));
        assertThat(unequipedItem.getEquipped(), is(false));
    }

    @Test
    void testEquipItemTheme(final GraphQlTester tester) {
        UUID colorThemeId = UUID.fromString(ItemUtil.COLOR_THEME_ID);
        UUID patternThemeId = UUID.fromString(ItemUtil.PATTERN_THEME_ID);
        UserEntity user = new UserEntity(loggedInUser.getId(), 0, null, null, null, null, new ArrayList<>(), new UserInventoryEntity(), new ArrayList<>(), null, new ArrayList<>());
        ItemInstanceEntity itemInstanceEntityPattern = new ItemInstanceEntity();
        itemInstanceEntityPattern.setPrototypeId(patternThemeId);
        itemInstanceEntityPattern.setEquipped(false);
        itemInstanceEntityPattern.setUniqueDescription("");
        itemInstanceEntityPattern.setItemType(ItemType.PatternTheme);
        user.getInventory().getItems().add(itemInstanceEntityPattern);
        ItemInstanceEntity itemInstanceEntityColor = new ItemInstanceEntity();
        itemInstanceEntityColor.setPrototypeId(colorThemeId);
        itemInstanceEntityColor.setEquipped(true);
        itemInstanceEntityColor.setUniqueDescription("");
        itemInstanceEntityColor.setItemType(ItemType.ColorTheme);
        user.getInventory().getItems().add(itemInstanceEntityColor);
        userRepository.save(user);

        final String query = """
                mutation {
                    equipItem(itemId: "%s")
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
                """.formatted(patternThemeId);

        Inventory inventory = tester.document(query)
                .execute()
                .path("equipItem").entity(Inventory.class).get();
        UserItem equipedItem = inventory.getItems().stream().filter(userItem -> userItem.getId().equals(patternThemeId)).findFirst().get();
        UserItem unequipedItem = inventory.getItems().stream().filter(userItem -> userItem.getId().equals(colorThemeId)).findFirst().get();
        assertThat(equipedItem.getEquipped(), is(true));
        assertThat(unequipedItem.getEquipped(), is(false));
        UserEntity userEntity = userRepository.findById(loggedInUser.getId()).get();
        ItemInstanceEntity equipedItemInstance = userEntity.getInventory().getItems().stream().filter(itemInstanceEntity1 -> itemInstanceEntity1.getPrototypeId().equals(patternThemeId)).findFirst().get();
        ItemInstanceEntity unequipedItemInstance = userEntity.getInventory().getItems().stream().filter(itemInstanceEntity1 -> itemInstanceEntity1.getPrototypeId().equals(colorThemeId)).findFirst().get();
        assertThat(equipedItemInstance.isEquipped(), is(true));
        assertThat(unequipedItemInstance.isEquipped(), is(false));
    }

    @Test
    void testEquipItemThemeSwitchedOrder(final GraphQlTester tester) {
        UUID colorThemeId = UUID.fromString(ItemUtil.COLOR_THEME_ID);
        UUID patternThemeId = UUID.fromString(ItemUtil.PATTERN_THEME_ID);
        UserEntity user = new UserEntity(loggedInUser.getId(), 0, null, null, null, null, new ArrayList<>(), new UserInventoryEntity(), new ArrayList<>(), null, new ArrayList<>());
        ItemInstanceEntity itemInstanceEntityPattern = new ItemInstanceEntity();
        itemInstanceEntityPattern.setPrototypeId(patternThemeId);
        itemInstanceEntityPattern.setEquipped(true);
        itemInstanceEntityPattern.setUniqueDescription("");
        itemInstanceEntityPattern.setItemType(ItemType.PatternTheme);
        user.getInventory().getItems().add(itemInstanceEntityPattern);
        ItemInstanceEntity itemInstanceEntityColor = new ItemInstanceEntity();
        itemInstanceEntityColor.setPrototypeId(colorThemeId);
        itemInstanceEntityColor.setEquipped(false);
        itemInstanceEntityColor.setUniqueDescription("");
        itemInstanceEntityColor.setItemType(ItemType.ColorTheme);
        user.getInventory().getItems().add(itemInstanceEntityColor);
        userRepository.save(user);

        final String query = """
                mutation {
                    equipItem(itemId: "%s")
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
                """.formatted(colorThemeId);

        Inventory inventory = tester.document(query)
                .execute()
                .path("equipItem").entity(Inventory.class).get();
        UserItem equipedItem = inventory.getItems().stream().filter(userItem -> userItem.getId().equals(colorThemeId)).findFirst().get();
        UserItem unequipedItem = inventory.getItems().stream().filter(userItem -> userItem.getId().equals(patternThemeId)).findFirst().get();
        assertThat(equipedItem.getEquipped(), is(true));
        assertThat(unequipedItem.getEquipped(), is(false));
        UserEntity userEntity = userRepository.findById(loggedInUser.getId()).get();
        ItemInstanceEntity equipedItemInstance = userEntity.getInventory().getItems().stream().filter(itemInstanceEntity1 -> itemInstanceEntity1.getPrototypeId().equals(colorThemeId)).findFirst().get();
        ItemInstanceEntity unequipedItemInstance = userEntity.getInventory().getItems().stream().filter(itemInstanceEntity1 -> itemInstanceEntity1.getPrototypeId().equals(patternThemeId)).findFirst().get();
        assertThat(equipedItemInstance.isEquipped(), is(true));
        assertThat(unequipedItemInstance.isEquipped(), is(false));
    }
}
