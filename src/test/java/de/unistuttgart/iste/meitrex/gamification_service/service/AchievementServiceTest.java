package de.unistuttgart.iste.meitrex.gamification_service.service;


import de.unistuttgart.iste.meitrex.common.dapr.TopicPublisher;
import de.unistuttgart.iste.meitrex.gamification_service.achievements.Achievements;
import de.unistuttgart.iste.meitrex.gamification_service.config.AdaptivityConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserCourseDataEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.AchievementRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.UserRepository;
import de.unistuttgart.iste.meitrex.generated.dto.Achievement;
import de.unistuttgart.iste.meitrex.generated.dto.Chapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static de.unistuttgart.iste.meitrex.gamification_service.service.AchievementService.mapUserGoalProgressToAchievements;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class AchievementServiceTest {
    private final UserService userService = mock(UserService.class);
    private final AchievementRepository achievementRepository = mock(AchievementRepository.class);
    private final AdaptivityConfiguration adaptivityConfiguration = mock(AdaptivityConfiguration.class);
    private final TopicPublisher topicPublisher = mock(TopicPublisher.class);

    private final Achievements achievements = new Achievements();

    AchievementService achievementService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        achievementService = new AchievementService(userService, achievementRepository,
                adaptivityConfiguration, topicPublisher);
        when(adaptivityConfiguration.getMaxAdaptiveAchievementCount()).thenReturn(10);
    }

    @Test
    void testGetAchievementsForUserInCourse() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        Chapter chapter = new Chapter();
        chapter.setId(UUID.randomUUID());
        chapter.setTitle("Chapter Title");
        chapter.setDescription("Chapter Description");
        List<Chapter> chapters = new ArrayList<>(List.of(chapter));
        CourseEntity courseEntity = CourseEntity.builder()
                .id(courseId)
                .chapters(chapters)
                .build();
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        courseEntity.setAchievements(achievementEntities);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        List<UserGoalProgressEntity> userGoalProgress = achievementEntities.stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity)).toList();

        UserCourseDataEntity courseData = UserCourseDataEntity.builder()
                .courseId(courseId)
                .goalProgressEntities(userGoalProgress)
                .build();
        userEntity.setCourseData(List.of(courseData));

        when(userService.getUser(userId)).thenReturn(Optional.of(userEntity));
        List<Achievement> achievementResult = achievementService.getAchievementsForUserInCourse(userId, courseId);
        List<Achievement> achievementList = new ArrayList<>();
        mapUserGoalProgressToAchievements(
                userEntity.getCourseData(courseId).orElseThrow().getGoalProgressEntities(),
                achievementList);
        assertThat(achievementResult, is(achievementList));
    }

    @Test
    void testGetAchievementsForUser() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        Chapter chapter = new Chapter();
        chapter.setId(UUID.randomUUID());
        chapter.setTitle("Chapter Title");
        chapter.setDescription("Chapter Description");
        List<Chapter> chapters = new ArrayList<>(List.of(chapter));
        CourseEntity courseEntity = CourseEntity.builder()
                .id(courseId)
                .chapters(chapters)
                .build();
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        courseEntity.setAchievements(achievementEntities);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);

        List<UserGoalProgressEntity> userGoalProgress = achievementEntities.stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity)).toList();

        UserCourseDataEntity courseData = UserCourseDataEntity.builder()
                .courseId(courseId)
                .goalProgressEntities(userGoalProgress)
                .build();
        userEntity.setCourseData(List.of(courseData));

        when(userService.getUser(userId)).thenReturn(Optional.of(userEntity));
        List<Achievement> achievementResult = achievementService.getAchievementsForUser(userId);
        List<Achievement> achievementList = new ArrayList<>();
        mapUserGoalProgressToAchievements(
                userEntity.getCourseData(courseId).orElseThrow().getGoalProgressEntities(),
                achievementList);
        assertThat(achievementResult, is(achievementList));
    }
}
