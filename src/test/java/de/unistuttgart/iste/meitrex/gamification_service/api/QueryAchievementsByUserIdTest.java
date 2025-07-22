package de.unistuttgart.iste.meitrex.gamification_service.api;

import de.unistuttgart.iste.meitrex.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.meitrex.common.testutil.InjectCurrentUserHeader;
import de.unistuttgart.iste.meitrex.common.testutil.MockTestPublisherConfiguration;
import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.course_service.client.CourseServiceClient;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.AchievementRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.UserGoalProgressRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.UserRepository;
import de.unistuttgart.iste.meitrex.gamification_service.test_util.CourseMembershipUtil;
import de.unistuttgart.iste.meitrex.gamification_service.test_util.CourseUtil;
import de.unistuttgart.iste.meitrex.generated.dto.Achievement;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private UserGoalProgressRepository userGoalProgressRepository;
    @Autowired
    private AchievementRepository achievementRepository;

    @Test
    void queryAchievementsByUserIdEmpty (GraphQlTester tester) {
        UserEntity user = new UserEntity();
        user.setId(loggedInUser.getId());
        user.setCourseIds(new ArrayList<>());
        userRepository.save(user);

        CourseEntity courseEntity = CourseUtil.dummyCourseEntity(courseId1, achievementRepository);
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
        UserEntity user = new UserEntity();
        user.setId(loggedInUser.getId());
        user.setCourseIds(new ArrayList<>(List.of(courseId1)));
        userRepository.save(user);

        CourseEntity courseEntity = CourseUtil.dummyCourseEntity(courseId1, achievementRepository);
        courseRepository.save(courseEntity);

        List<UserGoalProgressEntity> userGoalProgressEntities = new ArrayList<>();
        for (AchievementEntity achievement : courseEntity.getAchievements()) {
            UserGoalProgressEntity userGoalProgressEntity = achievement.getGoal().generateUserGoalProgress(user);
            userGoalProgressEntities.add(userGoalProgressEntity);
        }
        userGoalProgressRepository.saveAll(userGoalProgressEntities);
        user.setUserGoalProgressEntities(userGoalProgressEntities);
        userRepository.save(user);

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
        assertThat(achievements, hasSize(courseEntity.getAchievements().size()));
    }

    @Test
    void queryAchievementsByUserIdTwoCourses (GraphQlTester tester) {
        UserEntity user = new UserEntity();
        user.setId(loggedInUser.getId());
        user.setCourseIds(new ArrayList<>(List.of(courseId1, courseId2)));
        userRepository.save(user);

        CourseEntity courseEntity1 = CourseUtil.dummyCourseEntity(courseId1, achievementRepository);
        courseRepository.save(courseEntity1);
        CourseEntity courseEntity2 = CourseUtil.dummyCourseEntity(courseId2, achievementRepository);
        courseRepository.save(courseEntity2);

        List<UserGoalProgressEntity> userGoalProgressEntities = new ArrayList<>();
        for (AchievementEntity achievement : courseEntity1.getAchievements()) {
            UserGoalProgressEntity userGoalProgressEntity = achievement.getGoal().generateUserGoalProgress(user);
            userGoalProgressEntities.add(userGoalProgressEntity);
        }
        for (AchievementEntity achievement : courseEntity2.getAchievements()) {
            UserGoalProgressEntity userGoalProgressEntity = achievement.getGoal().generateUserGoalProgress(user);
            userGoalProgressEntities.add(userGoalProgressEntity);
        }
        userGoalProgressRepository.saveAll(userGoalProgressEntities);
        user.setUserGoalProgressEntities(userGoalProgressEntities);
        userRepository.save(user);

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
        assertThat(achievements, hasSize(courseEntity1.getAchievements().size() + courseEntity2.getAchievements().size()));
    }
}

