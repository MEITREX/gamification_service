package de.unistuttgart.iste.meitrex.gamification_service.service;

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
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.UserInventoryEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.AchievementRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.UserGoalProgressRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.UserRepository;
import de.unistuttgart.iste.meitrex.generated.dto.Chapter;
import de.unistuttgart.iste.meitrex.generated.dto.CompositeProgressInformation;
import de.unistuttgart.iste.meitrex.generated.dto.Content;
import de.unistuttgart.iste.meitrex.generated.dto.ContentType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GoalProgressService {
    private final AchievementRepository achievementRepository;
    private final ContentServiceClient contentServiceClient;
    private final CourseServiceClient courseServiceClient;
    private final CourseRepository courseRepository;
    private final Achievements achievements = new Achievements();
    private final UserRepository userRepository;
    private final UserGoalProgressRepository userGoalProgressRepository;

    /**
     * Gets user progress according to the given event.
     * Updates the effected achievements
     *
     * @param contentProgressedEvent the event to log
     */
    public void progressUserProgress(final ContentProgressedEvent contentProgressedEvent) {
        UUID userId = contentProgressedEvent.getUserId();
        Content content;
        try {
            content = contentServiceClient.queryContentsByIds(userId,
                    List.of(contentProgressedEvent.getContentId())).getFirst();
        } catch (ContentServiceConnectionException e) {
            throw new RuntimeException(e);
        }
        UUID courseId = content.getMetadata().getCourseId();
        log.info("Course exists: {}", courseRepository.findById(courseId).isPresent());
        CourseEntity courseEntity = courseRepository.findById(courseId).orElseGet(() -> createCourse(courseId));
        UserEntity user = userRepository.findById(userId).orElseGet(() -> createUser(userId));
        if (!user.getCourseIds().contains(courseEntity.getId())) {
            addUserToCourse(courseEntity, user);
        }
        log.info("User {} ", user);
        if (Objects.requireNonNull(content.getMetadata().getType()) == ContentType.QUIZ) {
            quizProgress(contentProgressedEvent, user, courseId);
        }
    }

    private void quizProgress(final ContentProgressedEvent contentProgressedEvent, UserEntity user, UUID courseId) {
        log.info("Quiz progress");
        CompletedQuizzesGoalProgressEvent completedQuizzesGoalProgressEvent =
                getCompletedQuizzesGoalProgressEvent(contentProgressedEvent, user);
        user.getUserGoalProgressEntities().stream()
                .filter(userGoalProgressEntity ->
                        checkUserGoalProgressInCourse(courseId, userGoalProgressEntity))
                .forEach(goalProgressEntity -> {
                    goalProgressEntity.updateProgress(completedQuizzesGoalProgressEvent);
                });
        userRepository.save(user);
    }

    private static boolean checkUserGoalProgressInCourse(UUID courseId, UserGoalProgressEntity userGoalProgressEntity) {
        if (userGoalProgressEntity.getGoal().getParentWithGoal() instanceof AchievementEntity achievement) {
            return achievement.getCourse().getId().equals(courseId);
        } else {
            return false;
        }
    }

    @NotNull
    private static CompletedQuizzesGoalProgressEvent getCompletedQuizzesGoalProgressEvent(
            ContentProgressedEvent contentProgressedEvent, UserEntity user) {
        UUID contendId = contentProgressedEvent.getContentId();
        float correctness = (float) contentProgressedEvent.getCorrectness();
        CompletedQuizzesGoalProgressEvent completedQuizzesGoalProgressEvent = new CompletedQuizzesGoalProgressEvent();
        completedQuizzesGoalProgressEvent.setProgressType(ProgressType.QUIZ);
        completedQuizzesGoalProgressEvent.setUserId(user.getId());
        completedQuizzesGoalProgressEvent.setCourseId(contendId);
        completedQuizzesGoalProgressEvent.setScore(correctness);
        completedQuizzesGoalProgressEvent.setContentId(contendId);
        return completedQuizzesGoalProgressEvent;
    }

    public void chapterProgress(final UserProgressUpdatedEvent userProgressUpdatedEvent) {
        UUID courseId = userProgressUpdatedEvent.getCourseId();
        CourseEntity courseEntity = courseRepository.findById(courseId).orElseGet(() -> createCourse(courseId));
        UUID userId = userProgressUpdatedEvent.getUserId();
        UUID chapterId = userProgressUpdatedEvent.getChapterId();
        UserEntity user = userRepository.findById(userId).orElseGet(() -> createUser(userId));
        if (!user.getCourseIds().contains(courseEntity.getId())) {
            addUserToCourse(courseEntity, user);
        }
        try {
            CompositeProgressInformation progressInformation =
                    contentServiceClient.queryProgressByChapterId(userId, chapterId);
            if (progressInformation.getCompletedContents() == progressInformation.getTotalContents()) {
                CompleteSpecificChapterGoalProgressEvent completeSpecificChapterGoalProgressEvent =
                        getCompleteSpecificChapterGoalProgressEvent(userId, chapterId, courseId);
                user.getUserGoalProgressEntities().stream()
                        .filter(userGoalProgressEntity ->
                                checkUserGoalProgressInCourse(courseId, userGoalProgressEntity))
                        .forEach(goalProgressEntity -> {
                            goalProgressEntity.updateProgress(completeSpecificChapterGoalProgressEvent);
                        });
                userRepository.save(user);
            }
        } catch (ContentServiceConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static CompleteSpecificChapterGoalProgressEvent getCompleteSpecificChapterGoalProgressEvent(UUID userId,
                                                                                    UUID chapterId, UUID courseId) {
        CompleteSpecificChapterGoalProgressEvent completeSpecificChapterGoalProgressEvent =
                new CompleteSpecificChapterGoalProgressEvent();
        completeSpecificChapterGoalProgressEvent.setProgressType(ProgressType.CHAPTER);
        completeSpecificChapterGoalProgressEvent.setUserId(userId);
        completeSpecificChapterGoalProgressEvent.setChapterId(chapterId);
        completeSpecificChapterGoalProgressEvent.setCourseId(courseId);
        return completeSpecificChapterGoalProgressEvent;
    }

    public void forumProgress(final ForumActivityEvent forumActivityEvent) {
        UUID courseId = forumActivityEvent.getCourseId();
        CourseEntity courseEntity = courseRepository.findById(courseId).orElseGet(() -> createCourse(courseId));
        log.info(courseEntity.toString());
        UUID userId = forumActivityEvent.getUserId();
        UserEntity user = userRepository.findById(userId).orElseGet(() -> createUser(userId));
        if (!user.getCourseIds().contains(courseEntity.getId())) {
            addUserToCourse(courseEntity, user);
        }
        if (forumActivityEvent.getActivity() == ForumActivity.ANSWER) {
            forumAnswerProgress(user, courseId);
        }
    }

    private void forumAnswerProgress(UserEntity user, UUID courseId) {
        GoalProgressEvent goalProgressEvent = new GoalProgressEvent();
        goalProgressEvent.setUserId(user.getId());
        goalProgressEvent.setCourseId(courseId);
        goalProgressEvent.setProgressType(ProgressType.FORUM);
        user.getUserGoalProgressEntities().stream()
                .filter(userGoalProgressEntity ->
                        checkUserGoalProgressInCourse(courseId, userGoalProgressEntity))
                .forEach(userGoalProgressEntity -> {
                    userGoalProgressEntity.updateProgress(goalProgressEvent);
                });
        userRepository.save(user);
    }

    public UUID loginUser(UUID userId, UUID courseId) {
        CourseEntity courseEntity = courseRepository.findById(courseId).orElseGet(() -> createCourse(courseId));
        UserEntity user = userRepository.findById(userId).orElseGet(() -> createUser(userId));
        if (!user.getCourseIds().contains(courseEntity.getId())) {
            addUserToCourse(courseEntity, user);
        }
        LoginStreakGoalProgressEvent loginStreakGoalProgressEvent = getLoginStreakGoalProgressEvent(userId, courseId);
        user.getUserGoalProgressEntities().stream()
                .filter(userGoalProgressEntity ->
                        checkUserGoalProgressInCourse(courseId, userGoalProgressEntity))
                .forEach(userGoalProgressEntity -> {
                    userGoalProgressEntity.updateProgress(loginStreakGoalProgressEvent);
                });
        userRepository.save(user);
        return userId;
    }

    @NotNull
    private static LoginStreakGoalProgressEvent getLoginStreakGoalProgressEvent(UUID userId, UUID courseId) {
        LoginStreakGoalProgressEvent loginStreakGoalProgressEvent = new LoginStreakGoalProgressEvent();
        loginStreakGoalProgressEvent.setUserId(userId);
        loginStreakGoalProgressEvent.setCourseId(courseId);
        loginStreakGoalProgressEvent.setProgressType(ProgressType.LOGIN);
        loginStreakGoalProgressEvent.setLoginTime(OffsetDateTime.now());
        return loginStreakGoalProgressEvent;
    }

    private CourseEntity createCourse(final UUID courseId) {
        log.info("try to create course {}", courseId);
        List<Chapter> chapters = courseServiceClient.queryChapterByCourseId(courseId);
        CourseEntity courseEntity = new CourseEntity(courseId, chapters);
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        achievementRepository.saveAll(achievementEntities);
        courseEntity.setAchievements(achievementEntities);
        courseEntity = courseRepository.save(courseEntity);
        log.info("Created course with id {}", courseId);
        log.info("Created course {}", courseEntity);
        return courseEntity;
    }

    private void addUserToCourse(final CourseEntity course, UserEntity user) {
        log.info("add user to course {}", course.getId());
        user.getCourseIds().add(course.getId());
        userRepository.saveAndFlush(user);
        List<UserGoalProgressEntity> userGoalProgressEntities = new ArrayList<>();
        for (AchievementEntity achievement : course.getAchievements()) {
            UserGoalProgressEntity userGoalProgressEntity = achievement.getGoal().generateUserGoalProgress(user);
            userGoalProgressEntities.add(userGoalProgressEntity);
        }
        userGoalProgressRepository.saveAll(userGoalProgressEntities);
        user.getUserGoalProgressEntities().addAll(userGoalProgressEntities);
        user = userRepository.save(user);
        log.info("Added user to course {}", user);
    }

    public UserEntity createUser(final UUID userId) {
        UserEntity userEntity = new UserEntity(userId, new ArrayList<>(), new ArrayList<>(), new UserInventoryEntity());
        userEntity = userRepository.save(userEntity);
        log.info("Created user with id {}", userId);
        log.info("Created user {}", userEntity);
        return userEntity;
    }
}
