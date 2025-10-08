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
import de.unistuttgart.iste.meitrex.gamification_service.test_util.MockKeycloakClientConfig;
import de.unistuttgart.iste.meitrex.generated.dto.Inventory;
import de.unistuttgart.iste.meitrex.generated.dto.UserItem;
import de.unistuttgart.iste.meitrex.generated.dto.UserItemComplete;
import jakarta.transaction.Transactional;
import org.apache.catalina.User;
import org.hamcrest.Matchers;
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

@ContextConfiguration(classes = {MockTestPublisherConfiguration.class, MockContentServiceClientConfiguration.class, MockCourseServiceClientConfiguration.class, MockKeycloakClientConfig.class})
@GraphQlApiTest
@Transactional
@ActiveProfiles("test")
public class MutationLotteryRunTest {

    UUID courseId = UUID.randomUUID();

    private final static int LOTTERY_COST = 3000;

    @Autowired
    DefaultItemService itemService;

    @InjectCurrentUserHeader
    private final LoggedInUser loggedInUser = userWithMembershipInCourseWithId(courseId, LoggedInUser.UserRoleInCourse.STUDENT);
    @Autowired
    private IUserRepository userRepository;

    @Test
    void testRunLottery(final GraphQlTester tester) {
        int initialUnspentPoints = 4000;
        UserEntity user = new UserEntity(loggedInUser.getId(), 0, null, null, null, null, new ArrayList<>(), new UserInventoryEntity(), new ArrayList<>(), null, new ArrayList<>());
        user.getInventory().setUnspentPoints(initialUnspentPoints);
        userRepository.save(user);

        final String query = """
                mutation {
                    lotteryRun
                    {
                       id
                       equipped
                       unlocked
                       unlockedTime
                       name
                       description
                       rarity
                       moneyCost
                       sellCompensation
                       obtainableInLottery
                       obtainableAsReward
                       obtainableInShop
                       foreColor
                       backColor
                       url
                       filename
                       nickname
                       sold
                    }
                }
                """;

        UserItemComplete userItemComplete = tester.document(query)
                .execute()
                .path("lotteryRun").entity(UserItemComplete.class).get();
        UserEntity userEntity = userRepository.findById(loggedInUser.getId()).get();
        assertThat(userEntity.getInventory().getItems().stream().anyMatch(itemInstanceEntity
                -> itemInstanceEntity.getPrototypeId().equals(userItemComplete.getId())), is(true));
        assertThat(userItemComplete.getUnlockedTime().toLocalDate().getDayOfMonth(), is(OffsetDateTime.now().getDayOfMonth()));
        assertThat(userEntity.getInventory().getUnspentPoints(), is(initialUnspentPoints - LOTTERY_COST));
    }

    @Test
    void testRunLotteryTillItemSold(final GraphQlTester tester) {
        int initialUnspentPoints = 1000000;
        UserEntity user = new UserEntity(loggedInUser.getId(), 0, null, null, null, null, new ArrayList<>(), new UserInventoryEntity(), new ArrayList<>(), null, new ArrayList<>());
        user.getInventory().setUnspentPoints(initialUnspentPoints);
        userRepository.save(user);

        final String query = """
                mutation {
                    lotteryRun
                    {
                       id
                       equipped
                       unlocked
                       unlockedTime
                       name
                       description
                       rarity
                       moneyCost
                       sellCompensation
                       obtainableInLottery
                       obtainableAsReward
                       obtainableInShop
                       foreColor
                       backColor
                       url
                       filename
                       nickname
                       sold
                    }
                }
                """;

        boolean sold = false;
        UserItemComplete userItemComplete = new UserItemComplete();
        int counter = 0;
        while (!sold) {
            counter++;
            userItemComplete = tester.document(query)
                    .execute()
                    .path("lotteryRun").entity(UserItemComplete.class).get();
            sold = userItemComplete.getSold();
        }
        UserEntity userEntity = userRepository.findById(loggedInUser.getId()).get();
        UUID returnedItemId = userItemComplete.getId();
        assertThat(userEntity.getInventory().getItems().stream().anyMatch(itemInstanceEntity
                -> itemInstanceEntity.getPrototypeId().equals(returnedItemId)), is(true));
        assertThat(userEntity.getInventory().getUnspentPoints(), is(initialUnspentPoints + userItemComplete.getSellCompensation() - counter * LOTTERY_COST));
    }

    @Test
    void testRunLotteryNotEnoughMoney(final GraphQlTester tester) {
        int initialUnspentPoints = 1000;
        UserEntity user = new UserEntity(loggedInUser.getId(), 0, null, null, null, null, new ArrayList<>(), new UserInventoryEntity(), new ArrayList<>(), null, new ArrayList<>());
        user.getInventory().setUnspentPoints(initialUnspentPoints);
        userRepository.save(user);

        final String query = """
                mutation {
                    lotteryRun
                    {
                       id
                       equipped
                       unlocked
                       unlockedTime
                       name
                       description
                       rarity
                       moneyCost
                       sellCompensation
                       obtainableInLottery
                       obtainableAsReward
                       obtainableInShop
                       foreColor
                       backColor
                       url
                       filename
                       nickname
                       sold
                    }
                }
                """;

        tester.document(query)
                .execute()
                .path("lotteryRun").valueIsNull();
        assertThat(userRepository.findById(loggedInUser.getId()).get().getInventory().getUnspentPoints(), is(initialUnspentPoints));
    }
}
