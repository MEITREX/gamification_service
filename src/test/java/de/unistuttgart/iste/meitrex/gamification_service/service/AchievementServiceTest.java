package de.unistuttgart.iste.meitrex.gamification_service.service;


import de.unistuttgart.iste.meitrex.common.event.ContentProgressedEvent;
import de.unistuttgart.iste.meitrex.common.event.ForumActivity;
import de.unistuttgart.iste.meitrex.common.event.ForumActivityEvent;
import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.course_service.client.CourseServiceClient;
import de.unistuttgart.iste.meitrex.gamification_service.achievements.Achievements;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.*;
import de.unistuttgart.iste.meitrex.generated.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class AchievementServiceTest {
    private final AchievementRepository achievementRepository = mock(AchievementRepository.class);

    private final ContentServiceClient contentServiceClient = mock(ContentServiceClient.class);
    private final CourseServiceClient courseServiceClient = mock(CourseServiceClient.class);
    private final CourseRepository courseRepository  = mock(CourseRepository.class);
    private final CompletedQuizzesGoalRepository completedQuizzesGoalRepository  = mock(CompletedQuizzesGoalRepository.class);
    private final UserRepository userRepository  = mock(UserRepository.class);
    private final UserGoalProgressRepository userGoalProgressRepository  = mock(UserGoalProgressRepository.class);
    private final ModelMapper modelMapper = new ModelMapper();
    private final GoalRepository goalRepository  = mock(GoalRepository.class);

    private final Achievements achievements = new Achievements();

    AchievementService achievementService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        achievementService = new AchievementService(achievementRepository, contentServiceClient, courseServiceClient,
                courseRepository, completedQuizzesGoalRepository ,userRepository, userGoalProgressRepository,
                modelMapper, goalRepository);
    }

    @Test
    void testProgressUserProgressUserCourseEmpty() throws ContentServiceConnectionException {
        UUID contentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        ContentProgressedEvent contentProgressedEvent = ContentProgressedEvent.builder()
                .contentId(contentId)
                .userId(userId)
                .correctness(1)
                .success(true)
                .build();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        ContentMetadata contentMetadata = ContentMetadata.builder()
                .setCourseId(courseId)
                .setType(ContentType.QUIZ).build();
        Content content = MediaContent.builder()
                .setId(contentId)
                .setMetadata(contentMetadata).build();
        when(contentServiceClient.queryContentsByIds(userId, List.of(contentId))).thenReturn(List.of(content));
        achievementService.progessUserProgress(contentProgressedEvent);
        verify(userRepository).findById(userId);
        verify(courseRepository).findById(courseId);
    }

    @Test
    void testProgressUserProgressWithCourseEmptyUser() throws ContentServiceConnectionException {
        UUID contentId = UUID.randomUUID();
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
        achievements.generateAchievements(courseEntity);
        ContentProgressedEvent contentProgressedEvent = ContentProgressedEvent.builder()
                .contentId(contentId)
                .userId(userId)
                .correctness(1)
                .success(true)
                .build();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        ContentMetadata contentMetadata = ContentMetadata.builder()
                .setCourseId(courseId)
                .setType(ContentType.QUIZ).build();
        Content content = MediaContent.builder()
                .setId(contentId)
                .setMetadata(contentMetadata).build();
        when(contentServiceClient.queryContentsByIds(userId, List.of(contentId))).thenReturn(List.of(content));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
        achievementService.progessUserProgress(contentProgressedEvent);
        verify(userRepository).findById(userId);
        verify(courseRepository).findById(courseId);
        List<AchievementEntity> achievementEntities = courseEntity.getAchievements();
        List<UserGoalProgressEntity> userGoalProgress = achievementEntities.stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity)).toList();
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        verify(userRepository).saveAndFlush(userEntity);
    }

    @Test
    void testProgressUserProgressWithCourseAndUser() throws ContentServiceConnectionException {
        UUID contentId = UUID.randomUUID();
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
        achievements.generateAchievements(courseEntity);
        ContentProgressedEvent contentProgressedEvent = ContentProgressedEvent.builder()
                .contentId(contentId)
                .userId(userId)
                .correctness(1)
                .success(true)
                .build();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        ContentMetadata contentMetadata = ContentMetadata.builder()
                .setCourseId(courseId)
                .setType(ContentType.QUIZ).build();
        Content content = MediaContent.builder()
                .setId(contentId)
                .setMetadata(contentMetadata).build();
        List<AchievementEntity> achievementEntities = courseEntity.getAchievements();
        List<UserGoalProgressEntity> userGoalProgress = achievementEntities.stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity)).toList();
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        when(contentServiceClient.queryContentsByIds(userId, List.of(contentId))).thenReturn(List.of(content));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        achievementService.progessUserProgress(contentProgressedEvent);
        verify(userRepository).findById(userId);
        verify(courseRepository).findById(courseId);
        verify(userRepository).saveAndFlush(userEntity);
    }

    @Test
    void testForumProgressUserCourseEmpty() throws ContentServiceConnectionException {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID forumId = UUID.randomUUID();
        ForumActivityEvent forumActivityEvent = ForumActivityEvent.builder()
                .userId(userId)
                .forumId(forumId)
                .courseId(courseId)
                .activity(ForumActivity.ANSWER)
                .build();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        Chapter chapter = new Chapter();
        chapter.setId(UUID.randomUUID());
        chapter.setTitle("Chapter Title");
        chapter.setDescription("Chapter Description");
        List<Chapter> chapters = new ArrayList<>(List.of(chapter));
        when(courseServiceClient.queryChapterByCourseId(courseId)).thenReturn(chapters);
        achievementService.forumProgress(forumActivityEvent);
        verify(userRepository).findById(userId);
        verify(courseRepository).findById(courseId);
    }

    @Test
    void testForumProgressWithCourseEmptyUser() throws ContentServiceConnectionException {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID forumId = UUID.randomUUID();
        Chapter chapter = new Chapter();
        chapter.setId(UUID.randomUUID());
        chapter.setTitle("Chapter Title");
        chapter.setDescription("Chapter Description");
        List<Chapter> chapters = new ArrayList<>(List.of(chapter));
        CourseEntity courseEntity = CourseEntity.builder()
                .id(courseId)
                .chapters(chapters)
                .build();
        achievements.generateAchievements(courseEntity);
        ForumActivityEvent forumActivityEvent = ForumActivityEvent.builder()
                .userId(userId)
                .forumId(forumId)
                .courseId(courseId)
                .activity(ForumActivity.ANSWER)
                .build();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
        achievementService.forumProgress(forumActivityEvent);
        verify(userRepository).findById(userId);
        verify(courseRepository).findById(courseId);
        List<AchievementEntity> achievementEntities = courseEntity.getAchievements();
        List<UserGoalProgressEntity> userGoalProgress = achievementEntities.stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity)).toList();
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        verify(userRepository, times(1)).saveAndFlush(userEntity);
    }

    @Test
    void testForumProgressWithCourseAndUser() throws ContentServiceConnectionException {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID forumId = UUID.randomUUID();
        Chapter chapter = new Chapter();
        chapter.setId(UUID.randomUUID());
        chapter.setTitle("Chapter Title");
        chapter.setDescription("Chapter Description");
        List<Chapter> chapters = new ArrayList<>(List.of(chapter));
        CourseEntity courseEntity = CourseEntity.builder()
                .id(courseId)
                .chapters(chapters)
                .build();
        achievements.generateAchievements(courseEntity);
        ForumActivityEvent forumActivityEvent = ForumActivityEvent.builder()
                .userId(userId)
                .forumId(forumId)
                .courseId(courseId)
                .activity(ForumActivity.ANSWER)
                .build();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        List<AchievementEntity> achievementEntities = courseEntity.getAchievements();
        List<UserGoalProgressEntity> userGoalProgress = achievementEntities.stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity)).toList();
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        achievementService.forumProgress(forumActivityEvent);
        verify(userRepository).findById(userId);
        verify(courseRepository).findById(courseId);
        List<CountableUserGoalProgressEntity> userGoalProgressEntities = userEntity.getUserGoalProgressEntities().stream()
                .filter(userGoalProgressEntity -> userGoalProgressEntity.getGoal() instanceof AnswerForumQuestionGoalEntity)
                .map(userGoalProgressEntity -> (CountableUserGoalProgressEntity) userGoalProgressEntity).toList();
        userGoalProgressEntities.forEach(userGoalProgressEntity -> {
            AnswerForumQuestionGoalEntity goalEntity = (AnswerForumQuestionGoalEntity) userGoalProgressEntity.getGoal();
            goalEntity.updateProgress(userGoalProgressEntity);
            userGoalProgressRepository.saveAndFlush(userGoalProgressEntity);
        });
        verify(userRepository).saveAndFlush(userEntity);
    }

    @Test
    void testLoginProgressUserCourseEmpty() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        achievementService.loginUser(userId, courseId);
        verify(userRepository).findById(userId);
        verify(courseRepository).findById(courseId);
    }

    @Test
    void testLoginProgressWithCourseEmptyUser() {
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
        achievements.generateAchievements(courseEntity);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
        UUID currentUserId = achievementService.loginUser(userId, courseId);
        verify(userRepository).findById(userId);
        verify(courseRepository).findById(courseId);
        assertThat(currentUserId, is(userId));
    }

    @Test
    void testLoginProgressWithCourseAndUser() throws ContentServiceConnectionException {
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
        achievements.generateAchievements(courseEntity);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        List<AchievementEntity> achievementEntities = courseEntity.getAchievements();
        List<UserGoalProgressEntity> userGoalProgress = achievementEntities.stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity)).toList();
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        UUID currentUserId = achievementService.loginUser(userId, courseId);
        verify(userRepository).findById(userId);
        verify(courseRepository).findById(courseId);
        assertThat(currentUserId, is(userId));
    }
}
