package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.common.event.ContentProgressedEvent;
import de.unistuttgart.iste.meitrex.common.event.ForumActivity;
import de.unistuttgart.iste.meitrex.common.event.ForumActivityEvent;
import de.unistuttgart.iste.meitrex.common.event.UserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.course_service.client.CourseServiceClient;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserCourseDataEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.HasGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.UserInventoryEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.UserRepository;
import de.unistuttgart.iste.meitrex.generated.dto.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class GoalProgressService {
    private final AchievementService achievementService;
    private final ContentServiceClient contentServiceClient;
    private final CourseServiceClient courseServiceClient;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

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
        addUserToCourseIfNotAlready(courseEntity, user);
        log.info("User {} ", user);
        if (Objects.requireNonNull(content.getMetadata().getType()) == ContentType.QUIZ) {
            quizProgress(contentProgressedEvent, user, courseId);
        }
        if(content instanceof Assessment) {
            CompletedSpecificAssessmentGoalProgressEvent completedSpecificAssessmentGoalProgressEvent
                    = CompletedSpecificAssessmentGoalProgressEvent.builder()
                            .userId(userId)
                            .assessmentId(content.getId())
                            .build();
            updateGoalProgressEntitiesForUser(user, courseId, completedSpecificAssessmentGoalProgressEvent);
        } else if(content instanceof MediaContent) {
            CompletedSpecificMediaContentGoalProgressEvent completedSpecificMediaContentGoalProgressEvent
                    = CompletedSpecificMediaContentGoalProgressEvent.builder()
                            .userId(userId)
                            .mediaContentId(content.getId())
                            .build();
            updateGoalProgressEntitiesForUser(user, courseId, completedSpecificMediaContentGoalProgressEvent);
        }
        userRepository.save(user);
    }

    private void quizProgress(final ContentProgressedEvent contentProgressedEvent, UserEntity user, UUID courseId) {
        log.info("Quiz progress");
        CompletedQuizzesGoalProgressEvent completedQuizzesGoalProgressEvent =
                getCompletedQuizzesGoalProgressEvent(contentProgressedEvent, user);
        updateGoalProgressEntitiesForUser(user, courseId, completedQuizzesGoalProgressEvent);
    }

    private static boolean checkUserGoalProgressInCourse(UUID courseId, UserGoalProgressEntity userGoalProgressEntity) {
        return userGoalProgressEntity.getGoal().getParentWithGoal().getCourse().getId().equals(courseId);
    }

    @NotNull
    private static CompletedQuizzesGoalProgressEvent getCompletedQuizzesGoalProgressEvent(
            ContentProgressedEvent contentProgressedEvent, UserEntity user) {
        UUID contendId = contentProgressedEvent.getContentId();
        float correctness = (float) contentProgressedEvent.getCorrectness();
        return CompletedQuizzesGoalProgressEvent.builder()
                .userId(user.getId())
                .score(correctness)
                .contentId(contendId)
                .build();
    }

    public void chapterProgress(final UserProgressUpdatedEvent userProgressUpdatedEvent) {
        UUID courseId = userProgressUpdatedEvent.getCourseId();
        CourseEntity courseEntity = courseRepository.findById(courseId).orElseGet(() -> createCourse(courseId));
        UUID userId = userProgressUpdatedEvent.getUserId();
        UUID chapterId = userProgressUpdatedEvent.getChapterId();
        UserEntity user = userRepository.findById(userId).orElseGet(() -> createUser(userId));
        addUserToCourseIfNotAlready(courseEntity, user);
        try {
            CompositeProgressInformation progressInformation =
                    contentServiceClient.queryProgressByChapterId(userId, chapterId);
            if (progressInformation.getCompletedContents() == progressInformation.getTotalContents()) {
                CompletedSpecificChapterGoalProgressEvent completedSpecificChapterGoalProgressEvent =
                        getCompleteSpecificChapterGoalProgressEvent(userId, chapterId, courseId);
                updateGoalProgressEntitiesForUser(user, courseId, completedSpecificChapterGoalProgressEvent);
                userRepository.save(user);
            }
        } catch (ContentServiceConnectionException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static CompletedSpecificChapterGoalProgressEvent getCompleteSpecificChapterGoalProgressEvent(UUID userId,
                                                                                                         UUID chapterId, UUID courseId) {
        return CompletedSpecificChapterGoalProgressEvent.builder()
                .userId(userId)
                .chapterId(chapterId)
                .build();
    }

    public void forumProgress(final ForumActivityEvent forumActivityEvent) {
        UUID courseId = forumActivityEvent.getCourseId();
        CourseEntity courseEntity = courseRepository.findById(courseId).orElseGet(() -> createCourse(courseId));
        log.info(courseEntity.toString());
        UUID userId = forumActivityEvent.getUserId();
        UserEntity user = userRepository.findById(userId).orElseGet(() -> createUser(userId));
        addUserToCourseIfNotAlready(courseEntity, user);
        if (forumActivityEvent.getActivity() == ForumActivity.ANSWER) {
            forumAnswerProgress(user, courseId);
            userRepository.save(user);
        }
    }

    private void forumAnswerProgress(UserEntity user, UUID courseId) {
        GoalProgressEvent goalProgressEvent = AnswerForumGoalProgressEvent.builder()
                .userId(user.getId())
                .build();
        updateGoalProgressEntitiesForUser(user, courseId, goalProgressEvent);
    }

    public UUID loginUser(UUID userId, UUID courseId) {
        CourseEntity courseEntity = courseRepository.findById(courseId).orElseGet(() -> createCourse(courseId));
        UserEntity user = userRepository.findById(userId).orElseGet(() -> createUser(userId));
        addUserToCourseIfNotAlready(courseEntity, user);
        LoginStreakGoalProgressEvent loginStreakGoalProgressEvent = getLoginStreakGoalProgressEvent(userId, courseId);
        updateGoalProgressEntitiesForUser(user, courseId, loginStreakGoalProgressEvent);
        userRepository.save(user);
        return userId;
    }

    @NotNull
    private static LoginStreakGoalProgressEvent getLoginStreakGoalProgressEvent(UUID userId, UUID courseId) {
        return LoginStreakGoalProgressEvent.builder()
                .userId(userId)
                .loginTime(OffsetDateTime.now())
                .build();
    }

    protected CourseEntity createCourse(final UUID courseId) {
        log.info("try to create course {}", courseId);
        List<Chapter> chapters = courseServiceClient.queryChapterByCourseId(courseId);
        CourseEntity courseEntity = new CourseEntity(courseId, chapters);
        achievementService.createInitialAchievementsInCourseEntity(courseEntity);
        courseEntity = courseRepository.save(courseEntity);
        log.info("Created course with id {}", courseId);
        log.info("Created course {}", courseEntity);
        return courseEntity;
    }

    private UserCourseDataEntity addUserToCourseIfNotAlready(final CourseEntity course, UserEntity user) {
        Optional<UserCourseDataEntity> userCourseData = user.getCourseData(course.getId());

        if(userCourseData.isEmpty()) {
            log.info("add user to course {}", course.getId());

            List<UserGoalProgressEntity> userGoalProgressEntities = new ArrayList<>();
            for (AchievementEntity achievement : course.getAchievements()) {
                if (achievement.isAdaptive())
                    continue; // skip adaptive achievements, they are generated on demand

                UserGoalProgressEntity userGoalProgressEntity = achievement.getGoal().generateUserGoalProgress(user);
                userGoalProgressEntities.add(userGoalProgressEntity);
            }

            userCourseData = Optional.of(UserCourseDataEntity.builder()
                    .courseId(course.getId())
                    .goalProgressEntities(userGoalProgressEntities)
                    .dailyQuestSet(null)
                    .build());

            user.getCourseData().add(userCourseData.get());
            user = userRepository.save(user);
        }

        return userCourseData.get();
    }

    public UserEntity createUser(final UUID userId) {
        UserEntity userEntity = new UserEntity(userId, new ArrayList<>(), new UserInventoryEntity());
        userEntity = userRepository.save(userEntity);
        log.info("Created user with id {}", userId);
        log.info("Created user {}", userEntity);
        return userEntity;
    }

    private void updateGoalProgressEntitiesForUser(UserEntity user,
                                                   UUID courseId,
                                                   GoalProgressEvent goalProgressEvent) {
        List<UserGoalProgressEntity> completedGoals = user.getCourseData(courseId)
                .orElseThrow(() -> new IllegalArgumentException("updateGoalProgressEntitiesForUser(): " +
                        "User is not enrolled in course: " + courseId))
                .getGoalProgressEntities().stream()
                .filter(goalProgressEntity -> goalProgressEntity.updateProgress(goalProgressEvent))
                .toList();

        completedGoals.forEach(this::onGoalCompleted);
    }

    private void onGoalCompleted(UserGoalProgressEntity goalProgressEntity) {
        HasGoalEntity hasGoalEntity = Hibernate.unproxy(
                goalProgressEntity.getGoal().getParentWithGoal(),
                HasGoalEntity.class);
        log.info("onGoalCompleted(): Goal completed: {}", hasGoalEntity);
        if (hasGoalEntity instanceof AchievementEntity achievement) {
            achievementService.onAchievementCompleted(achievement, goalProgressEntity);
        }
    }
}
