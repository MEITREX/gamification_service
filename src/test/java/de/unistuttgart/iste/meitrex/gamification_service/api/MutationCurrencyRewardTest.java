package de.unistuttgart.iste.meitrex.gamification_service.api;

import de.unistuttgart.iste.meitrex.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.meitrex.common.testutil.InjectCurrentUserHeader;
import de.unistuttgart.iste.meitrex.common.testutil.MockTestPublisherConfiguration;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.UserInventoryEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserRepository;
import de.unistuttgart.iste.meitrex.gamification_service.test_config.MockContentServiceClientConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.test_config.MockCourseServiceClientConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.test_util.ItemUtil;
import de.unistuttgart.iste.meitrex.gamification_service.test_util.MockKeycloakClientConfig;
import de.unistuttgart.iste.meitrex.generated.dto.Inventory;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.UUID;

import static de.unistuttgart.iste.meitrex.common.testutil.TestUsers.userWithMembershipInCourseWithId;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ContextConfiguration(classes = {MockTestPublisherConfiguration.class, MockContentServiceClientConfiguration.class, MockCourseServiceClientConfiguration.class, MockKeycloakClientConfig.class})
@GraphQlApiTest
@Transactional
@ActiveProfiles("test")
public class MutationCurrencyRewardTest {
    UUID courseId = UUID.randomUUID();

    @InjectCurrentUserHeader
    private final LoggedInUser loggedInUser = userWithMembershipInCourseWithId(courseId, LoggedInUser.UserRoleInCourse.STUDENT);
    @Autowired
    private IUserRepository userRepository;

    @Test
    void testCurrencyRewardItem(final GraphQlTester tester) {
        UUID itemId = UUID.fromString(ItemUtil.DEFAULT_TUTOR_ITEM_ID);
        UserEntity user = new UserEntity(loggedInUser.getId(), 0, null, null, null, null, new ArrayList<>(), new UserInventoryEntity(), new ArrayList<>(), null, new ArrayList<>());
        user.getInventory().setUnspentPoints(0);
        userRepository.save(user);

        final String query = """
                mutation {
                    currencyReward(points: %s)
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
                """.formatted(10);

        Inventory inventory = tester.document(query)
                .execute()
                .path("currencyReward").entity(Inventory.class).get();
        assertThat(inventory.getUnspentPoints(), is(10));
    }
}
