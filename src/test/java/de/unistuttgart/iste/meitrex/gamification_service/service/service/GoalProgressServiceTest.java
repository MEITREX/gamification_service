package de.unistuttgart.iste.meitrex.gamification_service.service.service;

import de.unistuttgart.iste.meitrex.common.event.ContentProgressedEvent;
import de.unistuttgart.iste.meitrex.common.event.ForumActivity;
import de.unistuttgart.iste.meitrex.common.event.ForumActivityEvent;
import de.unistuttgart.iste.meitrex.common.event.UserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.course_service.client.CourseServiceClient;
import de.unistuttgart.iste.meitrex.gamification_service.achievements.Achievements;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.CompleteSpecificChapterGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.CompletedQuizzesGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.ProgressType;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.AchievementRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.UserGoalProgressRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.UserRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.GoalProgressService;
import de.unistuttgart.iste.meitrex.generated.dto.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class GoalProgressServiceTest {

    private final AchievementRepository achievementRepository = mock(AchievementRepository.class);

    private final ContentServiceClient contentServiceClient = mock(ContentServiceClient.class);
    private final CourseServiceClient courseServiceClient = mock(CourseServiceClient.class);
    private final CourseRepository courseRepository  = mock(CourseRepository.class);
    private final UserRepository userRepository  = mock(UserRepository.class);
    private final UserGoalProgressRepository userGoalProgressRepository  = mock(UserGoalProgressRepository.class);

    private final Achievements achievements = new Achievements();

    GoalProgressService goalProgressService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        goalProgressService = new GoalProgressService(achievementRepository, contentServiceClient, courseServiceClient,
                courseRepository, userRepository, userGoalProgressRepository);
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
        UserEntity userEntity = new UserEntity(userId, new ArrayList<>(), new ArrayList<>());
        ContentMetadata contentMetadata = ContentMetadata.builder()
                .setCourseId(courseId)
                .setType(ContentType.QUIZ).build();
        Content content = MediaContent.builder()
                .setId(contentId)
                .setMetadata(contentMetadata).build();
        when(contentServiceClient.queryContentsByIds(userId, List.of(contentId))).thenReturn(List.of(content));
        Chapter chapter = new Chapter();
        chapter.setId(UUID.randomUUID());
        chapter.setTitle("Chapter Title");
        chapter.setDescription("Chapter Description");
        List<Chapter> chapters = new ArrayList<>(List.of(chapter));
        CourseEntity courseEntity = new CourseEntity(courseId, chapters);
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        courseEntity.setAchievements(achievementEntities);
        when(courseRepository.save(courseEntity)).thenReturn(courseEntity);
        when(courseServiceClient.queryChapterByCourseId(courseId)).thenReturn(chapters);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        goalProgressService.progressUserProgress(contentProgressedEvent);
        verify(userRepository).findById(userId);
        verify(courseRepository, times(2)).findById(courseId);
        List<UserGoalProgressEntity> userGoalProgress = courseEntity.getAchievements().stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity)).toList();
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        verify(courseRepository).save(courseEntity);
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        CompletedQuizzesGoalProgressEvent completedQuizzesGoalProgressEvent = getCompletedQuizzesGoalProgressEvent(contentProgressedEvent, userEntity, courseId);
        userEntity.getUserGoalProgressEntities().forEach(goalProgressEntity -> {
            goalProgressEntity.updateProgress(completedQuizzesGoalProgressEvent);
        });
        verify(userRepository, times(2)).save(userEntity);
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
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        courseEntity.setAchievements(achievementEntities);
        ContentProgressedEvent contentProgressedEvent = ContentProgressedEvent.builder()
                .contentId(contentId)
                .userId(userId)
                .correctness(1)
                .success(true)
                .build();
        UserEntity userEntity = new UserEntity(userId, new ArrayList<>(), new ArrayList<>());
        ContentMetadata contentMetadata = ContentMetadata.builder()
                .setCourseId(courseId)
                .setType(ContentType.QUIZ).build();
        Content content = MediaContent.builder()
                .setId(contentId)
                .setMetadata(contentMetadata).build();
        when(contentServiceClient.queryContentsByIds(userId, List.of(contentId))).thenReturn(List.of(content));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        goalProgressService.progressUserProgress(contentProgressedEvent);
        verify(userRepository).findById(userId);
        verify(courseRepository, times(2)).findById(courseId);
        List<UserGoalProgressEntity> userGoalProgress = courseEntity.getAchievements().stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity)).toList();
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        CompletedQuizzesGoalProgressEvent completedQuizzesGoalProgressEvent = getCompletedQuizzesGoalProgressEvent(contentProgressedEvent, userEntity, courseId);
        userEntity.getUserGoalProgressEntities().forEach(goalProgressEntity -> {
            goalProgressEntity.updateProgress(completedQuizzesGoalProgressEvent);
        });
        verify(userRepository, times(2)).save(userEntity);
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
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        courseEntity.setAchievements(achievementEntities);
        ContentProgressedEvent contentProgressedEvent = ContentProgressedEvent.builder()
                .contentId(contentId)
                .userId(userId)
                .correctness(1)
                .success(true)
                .build();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setCourseIds(new ArrayList<>(List.of(courseId)));
        ContentMetadata contentMetadata = ContentMetadata.builder()
                .setCourseId(courseId)
                .setType(ContentType.QUIZ).build();
        Content content = MediaContent.builder()
                .setId(contentId)
                .setMetadata(contentMetadata).build();
        List<UserGoalProgressEntity> userGoalProgress = achievementEntities.stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity)).toList();
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        when(contentServiceClient.queryContentsByIds(userId, List.of(contentId))).thenReturn(List.of(content));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        goalProgressService.progressUserProgress(contentProgressedEvent);
        verify(userRepository).findById(userId);
        verify(courseRepository, times(2)).findById(courseId);
        verify(userRepository).save(userEntity);
    }

    @Test
    void testProgressChapterProgressUserCourseEmpty() throws ContentServiceConnectionException {
        UUID contentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID chapterId = UUID.randomUUID();
        UserProgressUpdatedEvent userProgressUpdatedEvent = UserProgressUpdatedEvent.builder()
                .chapterId(chapterId)
                .courseId(courseId)
                .userId(userId)
                .build();
        UserEntity userEntity = new UserEntity(userId, new ArrayList<>(), new ArrayList<>());
        ContentMetadata contentMetadata = ContentMetadata.builder()
                .setCourseId(courseId)
                .setType(ContentType.QUIZ).build();
        Content content = MediaContent.builder()
                .setId(contentId)
                .setMetadata(contentMetadata).build();
        when(contentServiceClient.queryContentsByIds(userId, List.of(contentId))).thenReturn(List.of(content));
        Chapter chapter = new Chapter();
        chapter.setId(chapterId);
        chapter.setTitle("Chapter Title");
        chapter.setDescription("Chapter Description");
        List<Chapter> chapters = new ArrayList<>(List.of(chapter));
        when(courseServiceClient.queryChapterByCourseId(courseId)).thenReturn(chapters);
        CourseEntity courseEntity = new CourseEntity(courseId, chapters);
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        courseEntity.setAchievements(achievementEntities);
        when(courseRepository.save(courseEntity)).thenReturn(courseEntity);
        CompositeProgressInformation compositeProgressInformation = CompositeProgressInformation.builder()
                .setProgress(100.0)
                .setCompletedContents(2)
                .setTotalContents(2)
                .build();
        when(contentServiceClient.queryProgressByChapterId(userId, chapterId)).thenReturn(compositeProgressInformation);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        goalProgressService.chapterProgress(userProgressUpdatedEvent);
        verify(userRepository).findById(userId);
        verify(courseRepository, times(1)).findById(courseId);
        List<UserGoalProgressEntity> userGoalProgress = courseEntity.getAchievements().stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity)).toList();
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        verify(courseRepository).save(courseEntity);
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        CompleteSpecificChapterGoalProgressEvent completeSpecificChapterGoalProgressEvent = getCompleteSpecificChapterGoalProgressEvent(userProgressUpdatedEvent);
        userEntity.getUserGoalProgressEntities().forEach(goalProgressEntity -> {
            goalProgressEntity.updateProgress(completeSpecificChapterGoalProgressEvent);
        });
        verify(userRepository, times(2)).save(userEntity);
    }

    @Test
    void testProgressChapterProgressWithCourseEmptyUser() throws ContentServiceConnectionException {
        UUID chapterId = UUID.randomUUID();
        UUID contentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        Chapter chapter = new Chapter();
        chapter.setId(chapterId);
        chapter.setTitle("Chapter Title");
        chapter.setDescription("Chapter Description");
        List<Chapter> chapters = new ArrayList<>(List.of(chapter));
        CourseEntity courseEntity = CourseEntity.builder()
                .id(courseId)
                .chapters(chapters)
                .build();
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        courseEntity.setAchievements(achievementEntities);
        UserProgressUpdatedEvent userProgressUpdatedEvent = UserProgressUpdatedEvent.builder()
                .chapterId(chapterId)
                .courseId(courseId)
                .userId(userId)
                .build();
        UserEntity userEntity = new UserEntity(userId, new ArrayList<>(), new ArrayList<>());
        ContentMetadata contentMetadata = ContentMetadata.builder()
                .setCourseId(courseId)
                .setType(ContentType.QUIZ).build();
        Content content = MediaContent.builder()
                .setId(contentId)
                .setMetadata(contentMetadata).build();
        when(contentServiceClient.queryContentsByIds(userId, List.of(contentId))).thenReturn(List.of(content));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
        CompositeProgressInformation compositeProgressInformation = CompositeProgressInformation.builder()
                .setProgress(100.0)
                .setCompletedContents(2)
                .setTotalContents(2)
                .build();
        when(contentServiceClient.queryProgressByChapterId(userId, chapterId)).thenReturn(compositeProgressInformation);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        goalProgressService.chapterProgress(userProgressUpdatedEvent);
        verify(userRepository).findById(userId);
        verify(courseRepository, times(1)).findById(courseId);
        List<UserGoalProgressEntity> userGoalProgress = courseEntity.getAchievements().stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity)).toList();
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        CompleteSpecificChapterGoalProgressEvent completeSpecificChapterGoalProgressEvent = getCompleteSpecificChapterGoalProgressEvent(userProgressUpdatedEvent);
        userEntity.getUserGoalProgressEntities().forEach(goalProgressEntity -> {
            goalProgressEntity.updateProgress(completeSpecificChapterGoalProgressEvent);
        });
        verify(userRepository, times(2)).save(userEntity);
    }

    @Test
    void testProgressChapterProgressWithCourseAndUser() throws ContentServiceConnectionException {
        UUID chapterId = UUID.randomUUID();
        UUID contentId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        Chapter chapter = new Chapter();
        chapter.setId(chapterId);
        chapter.setTitle("Chapter Title");
        chapter.setDescription("Chapter Description");
        List<Chapter> chapters = new ArrayList<>(List.of(chapter));
        CourseEntity courseEntity = CourseEntity.builder()
                .id(courseId)
                .chapters(chapters)
                .build();
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        courseEntity.setAchievements(achievementEntities);
        UserProgressUpdatedEvent userProgressUpdatedEvent = UserProgressUpdatedEvent.builder()
                .chapterId(chapterId)
                .courseId(courseId)
                .userId(userId)
                .build();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setCourseIds(new ArrayList<>(List.of(courseId)));
        ContentMetadata contentMetadata = ContentMetadata.builder()
                .setCourseId(courseId)
                .setType(ContentType.QUIZ).build();
        Content content = MediaContent.builder()
                .setId(contentId)
                .setMetadata(contentMetadata).build();
        List<UserGoalProgressEntity> userGoalProgress = achievementEntities.stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity)).toList();
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        when(contentServiceClient.queryContentsByIds(userId, List.of(contentId))).thenReturn(List.of(content));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        CompositeProgressInformation compositeProgressInformation = CompositeProgressInformation.builder()
                .setProgress(100.0)
                .setCompletedContents(2)
                .setTotalContents(2)
                .build();
        when(contentServiceClient.queryProgressByChapterId(userId, chapterId)).thenReturn(compositeProgressInformation);
        goalProgressService.chapterProgress(userProgressUpdatedEvent);
        verify(userRepository).findById(userId);
        verify(courseRepository, times(1)).findById(courseId);
        verify(userRepository).save(userEntity);
    }

    @Test
    void testForumProgressUserCourseEmpty() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID forumId = UUID.randomUUID();
        ForumActivityEvent forumActivityEvent = ForumActivityEvent.builder()
                .userId(userId)
                .forumId(forumId)
                .courseId(courseId)
                .activity(ForumActivity.ANSWER)
                .build();
        UserEntity userEntity = new UserEntity(userId, new ArrayList<>(), new ArrayList<>());
        Chapter chapter = new Chapter();
        chapter.setId(UUID.randomUUID());
        chapter.setTitle("Chapter Title");
        chapter.setDescription("Chapter Description");
        List<Chapter> chapters = new ArrayList<>(List.of(chapter));
        CourseEntity courseEntity = new CourseEntity(courseId, chapters);
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        courseEntity.setAchievements(achievementEntities);
        when(courseRepository.save(courseEntity)).thenReturn(courseEntity);
        when(courseServiceClient.queryChapterByCourseId(courseId)).thenReturn(chapters);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        goalProgressService.forumProgress(forumActivityEvent);
        verify(userRepository).findById(userId);
        verify(courseRepository).findById(courseId);
    }

    @Test
    void testForumProgressWithCourseEmptyUser(){
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
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        courseEntity.setAchievements(achievementEntities);
        ForumActivityEvent forumActivityEvent = ForumActivityEvent.builder()
                .userId(userId)
                .forumId(forumId)
                .courseId(courseId)
                .activity(ForumActivity.ANSWER)
                .build();
        UserEntity userEntity = new UserEntity(userId, new ArrayList<>(), new ArrayList<>());
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        goalProgressService.forumProgress(forumActivityEvent);
        verify(userRepository).findById(userId);
        verify(courseRepository).findById(courseId);
        List<UserGoalProgressEntity> userGoalProgress = achievementEntities.stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity)).toList();
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        GoalProgressEvent goalProgressEvent = getForumGoalProgressEvent(forumActivityEvent);
        userEntity.getUserGoalProgressEntities().forEach(goalProgressEntity -> {
            goalProgressEntity.updateProgress(goalProgressEvent);
        });
        verify(userRepository, times(2)).save(userEntity);
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
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        courseEntity.setAchievements(achievementEntities);
        ForumActivityEvent forumActivityEvent = ForumActivityEvent.builder()
                .userId(userId)
                .forumId(forumId)
                .courseId(courseId)
                .activity(ForumActivity.ANSWER)
                .build();
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setCourseIds(new ArrayList<>(List.of(courseId)));
        List<UserGoalProgressEntity> userGoalProgress = achievementEntities.stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity)).toList();
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        goalProgressService.forumProgress(forumActivityEvent);
        verify(userRepository).findById(userId);
        verify(courseRepository).findById(courseId);

        verify(userRepository).save(userEntity);
    }

    @Test
    void testLoginProgressUserCourseEmpty() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        UserEntity userEntity = new UserEntity(userId, new ArrayList<>(), new ArrayList<>());
        Chapter chapter = new Chapter();
        chapter.setId(UUID.randomUUID());
        chapter.setTitle("Chapter Title");
        chapter.setDescription("Chapter Description");
        List<Chapter> chapters = new ArrayList<>(List.of(chapter));
        when(courseServiceClient.queryChapterByCourseId(courseId)).thenReturn(chapters);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        CourseEntity courseEntity = new CourseEntity(courseId, chapters);
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        courseEntity.setAchievements(achievementEntities);
        when(courseRepository.save(courseEntity)).thenReturn(courseEntity);
        goalProgressService.loginUser(userId, courseId);
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
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        courseEntity.setAchievements(achievementEntities);
        UserEntity userEntity = new UserEntity(userId, new ArrayList<>(), new ArrayList<>());
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        UUID currentUserId = goalProgressService.loginUser(userId, courseId);
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
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        courseEntity.setAchievements(achievementEntities);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setCourseIds(new ArrayList<>(List.of(courseId)));
        List<UserGoalProgressEntity> userGoalProgress = achievementEntities.stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity)).toList();
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(courseEntity));
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        UUID currentUserId = goalProgressService.loginUser(userId, courseId);
        verify(userRepository).findById(userId);
        verify(courseRepository).findById(courseId);
        assertThat(currentUserId, is(userId));
    }

    private static @NotNull GoalProgressEvent getForumGoalProgressEvent(ForumActivityEvent forumActivityEvent) {
        GoalProgressEvent goalProgressEvent = new GoalProgressEvent();
        goalProgressEvent.setProgressType(ProgressType.FORUM);
        goalProgressEvent.setUserId(forumActivityEvent.getUserId());
        goalProgressEvent.setCourseId(forumActivityEvent.getCourseId());
        return goalProgressEvent;
    }

    private static @NotNull CompletedQuizzesGoalProgressEvent getCompletedQuizzesGoalProgressEvent(ContentProgressedEvent contentProgressedEvent, UserEntity userEntity, UUID courseId) {
        UUID contendId = contentProgressedEvent.getContentId();
        float correctness = (float) contentProgressedEvent.getCorrectness();
        CompletedQuizzesGoalProgressEvent completedQuizzesGoalProgressEvent = new CompletedQuizzesGoalProgressEvent();
        completedQuizzesGoalProgressEvent.setProgressType(ProgressType.QUIZ);
        completedQuizzesGoalProgressEvent.setUserId(userEntity.getId());
        completedQuizzesGoalProgressEvent.setCourseId(courseId);
        completedQuizzesGoalProgressEvent.setScore(correctness);
        completedQuizzesGoalProgressEvent.setContentId(contendId);
        return completedQuizzesGoalProgressEvent;
    }

    private static @NotNull CompleteSpecificChapterGoalProgressEvent getCompleteSpecificChapterGoalProgressEvent(UserProgressUpdatedEvent userProgressUpdatedEvent) {
        CompleteSpecificChapterGoalProgressEvent completeSpecificChapterGoalProgressEvent = new CompleteSpecificChapterGoalProgressEvent();
        completeSpecificChapterGoalProgressEvent.setProgressType(ProgressType.CHAPTER);
        completeSpecificChapterGoalProgressEvent.setUserId(userProgressUpdatedEvent.getUserId());
        completeSpecificChapterGoalProgressEvent.setCourseId(userProgressUpdatedEvent.getCourseId());
        completeSpecificChapterGoalProgressEvent.setChapterId(userProgressUpdatedEvent.getChapterId());
        return completeSpecificChapterGoalProgressEvent;
    }
}
