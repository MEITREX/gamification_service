package de.unistuttgart.iste.meitrex.gamification_service.api;

import de.unistuttgart.iste.meitrex.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.meitrex.common.testutil.InjectCurrentUserHeader;
import de.unistuttgart.iste.meitrex.common.testutil.MockTestPublisherConfiguration;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.course_service.client.CourseServiceClient;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserCourseDataEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.UserInventoryEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.AchievementRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ICourseRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserGoalProgressRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserRepository;
import de.unistuttgart.iste.meitrex.gamification_service.test_util.CourseMembershipUtil;
import de.unistuttgart.iste.meitrex.gamification_service.test_util.CourseUtil;
import de.unistuttgart.iste.meitrex.generated.dto.Achievement;
import de.unistuttgart.iste.meitrex.generated.dto.UserItem;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static de.unistuttgart.iste.meitrex.common.testutil.TestUsers.userWithMemberships;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

@ContextConfiguration(classes = {MockTestPublisherConfiguration.class})
@GraphQlApiTest
@Import(CourseUtil.class)
@Transactional
@ActiveProfiles("test")
public class QueryAchievementsByUserIdTest {
    private final UUID courseId1 = UUID.randomUUID();
    private final UUID courseId2 = UUID.randomUUID();


    @Autowired
    CourseServiceClient courseServiceClient;

    @Autowired
    ContentServiceClient contentServiceClient;

    private final LoggedInUser.CourseMembership courseMembership1 = CourseMembershipUtil.dummyCourseMembershipBuilder(courseId1);
    private final LoggedInUser.CourseMembership courseMembership2 = CourseMembershipUtil.dummyCourseMembershipBuilder(courseId2);

    @InjectCurrentUserHeader
    private final LoggedInUser loggedInUser = userWithMemberships(courseMembership1, courseMembership2);
    @Autowired
    private IUserRepository userRepository;
    @Autowired
    private ICourseRepository courseRepository;
    @Autowired
    private IUserGoalProgressRepository userGoalProgressRepository;
    @Autowired
    private AchievementRepository achievementRepository;
    @Autowired
    private CourseUtil courseUtil;

    @Test
    void queryAchievementsByUserIdEmpty (GraphQlTester tester) {
        UserEntity user = new UserEntity(loggedInUser.getId(), 0, null, null, null, null, new ArrayList<>(), new UserInventoryEntity(), new ArrayList<>(), null, new ArrayList<>());
        userRepository.save(user);

        CourseEntity courseEntity = courseUtil.dummyCourseEntity(courseId1, achievementRepository);
        courseRepository.save(courseEntity);

        final String query = """
                query {
                    achievementsByUserId(userId: "%s") {
                        id
                        name
                        imageUrl
                        description
                        courseId
                        userId
                        completed
                        requiredCount
                        completedCount
                        trackingStartTime
                        trackingEndTime
                    }
                }
                """.formatted(loggedInUser.getId());
        List<Achievement> achievements = tester.document(query)
                .execute()
                .path("achievementsByUserId").entityList(Achievement.class).get();
        assertThat(achievements, hasSize(0));
    }

    @Test
    void queryAchievementsByUserId (GraphQlTester tester) {
        UserEntity user = new UserEntity(loggedInUser.getId(), 0, null, null, null, null, new ArrayList<>(), new UserInventoryEntity(), new ArrayList<>(), null, new ArrayList<>());
        userRepository.save(user);

        CourseEntity courseEntity = courseUtil.dummyCourseEntity(courseId1, achievementRepository);
        courseRepository.save(courseEntity);


        List<UserGoalProgressEntity> userGoalProgressEntities = new ArrayList<>();
        for (AchievementEntity achievement : courseEntity.getAchievements()) {
            UserGoalProgressEntity userGoalProgressEntity = achievement.getGoal().generateUserGoalProgress(user);
            userGoalProgressEntities.add(userGoalProgressEntity);
        }
        userGoalProgressRepository.saveAll(userGoalProgressEntities);
        UserCourseDataEntity userCourseDataEntity = new UserCourseDataEntity(null, courseId1, null, userGoalProgressEntities, null);
        user.getCourseData().add(userCourseDataEntity);
        userRepository.save(user);


        final String query = """
                query {
                    achievementsByUserId(userId: "%s") {
                        __typename
                        ... on Achievement {
                            id
                            name
                            imageUrl
                            description
                            courseId
                            userId
                            completed
                            requiredCount
                            completedCount
                            trackingStartTime
                            trackingEndTime
                        }
                    }
                }
                """.formatted(loggedInUser.getId());
        List<Achievement> achievements = tester.document(query)
                .execute()
                .path("achievementsByUserId").entityList(Achievement.class).get();
        assertThat(achievements, hasSize(courseEntity.getAchievements().size()));
    }

    @Test
    void queryAchievementsByUserIdTwoCourses (GraphQlTester tester) {
        UserEntity user = new UserEntity(loggedInUser.getId(), 0, null, null, null, null, new ArrayList<>(), new UserInventoryEntity(), new ArrayList<>(), null, new ArrayList<>());

        userRepository.save(user);

        CourseEntity courseEntity1 = courseUtil.dummyCourseEntity(courseId1, achievementRepository);
        courseRepository.save(courseEntity1);
        CourseEntity courseEntity2 = courseUtil.dummyCourseEntity(courseId2, achievementRepository);
        courseRepository.save(courseEntity2);

        List<UserGoalProgressEntity> userGoalProgressEntities1 = new ArrayList<>();
        for (AchievementEntity achievement : courseEntity1.getAchievements()) {
            UserGoalProgressEntity userGoalProgressEntity = achievement.getGoal().generateUserGoalProgress(user);
            userGoalProgressEntities1.add(userGoalProgressEntity);
        }

        List<UserGoalProgressEntity> userGoalProgressEntities2 = new ArrayList<>();
        for (AchievementEntity achievement : courseEntity2.getAchievements()) {
            UserGoalProgressEntity userGoalProgressEntity = achievement.getGoal().generateUserGoalProgress(user);
            userGoalProgressEntities2.add(userGoalProgressEntity);
        }
        userGoalProgressRepository.saveAll(userGoalProgressEntities1);
        userGoalProgressRepository.saveAll(userGoalProgressEntities2);
        UserCourseDataEntity userCourseDataEntity1 = new UserCourseDataEntity(null, courseId1, null, userGoalProgressEntities1, null);
        UserCourseDataEntity userCourseDataEntity2 = new UserCourseDataEntity(null, courseId1, null, userGoalProgressEntities2, null);
        user.getCourseData().add(userCourseDataEntity1);
        user.getCourseData().add(userCourseDataEntity2);
        userRepository.save(user);

        final String query = """
                query {
                    achievementsByUserId(userId: "%s") {
                        __typename
                        ... on Achievement {
                            id
                            name
                            imageUrl
                            description
                            courseId
                            userId
                            completed
                            requiredCount
                            completedCount
                            trackingStartTime
                            trackingEndTime
                        }
                    }
                }
                """.formatted(loggedInUser.getId());
        List<Achievement> achievements = tester.document(query)
                .execute()
                .path("achievementsByUserId").entityList(Achievement.class).get();
        assertThat(achievements, hasSize(courseEntity1.getAchievements().size() + courseEntity2.getAchievements().size()));
    }
}

