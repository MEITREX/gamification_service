package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.common.event.ContentProgressedEvent;
import de.unistuttgart.iste.meitrex.common.event.ForumActivityEvent;
import de.unistuttgart.iste.meitrex.common.event.UserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.course_service.client.CourseServiceClient;
import de.unistuttgart.iste.meitrex.gamification_service.achievements.Achievements;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.CountableUserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.*;
import de.unistuttgart.iste.meitrex.generated.dto.Achievement;
import de.unistuttgart.iste.meitrex.generated.dto.Content;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AchievementService {
    private final AchievementRepository achievementRepository;
    private final ContentServiceClient contentServiceClient;
    private final CourseServiceClient courseServiceClient;
    private final CourseRepository courseRepository;
    private final CompletedQuizzesGoalRepository completedQuizzesGoalRepository;
    private final Achievements achievements = new Achievements();
    private final UserRepository userRepository;
    private final UserGoalProgressRepository userGoalProgressRepository;
    private final ModelMapper modelMapper;
    private final GoalRepository goalRepository;

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
        UserEntity user = userRepository.findById(userId).orElse(generateUser(userId));
        if (!user.getCourseIds().contains(courseEntity.getId())) {
            addUserToCourse(courseEntity, user);
        }
        log.info("User {} ", user);
        switch (content.getMetadata().getType()) {
            case QUIZ -> quizProgress(contentProgressedEvent, user);
            case MEDIA -> mediaProgress(contentProgressedEvent, user);
        }
    }

    private void quizProgress(final ContentProgressedEvent contentProgressedEvent, UserEntity user) {
        log.info("Quiz progress");
        UUID contendId = contentProgressedEvent.getContentId();
        float correctness = (float)contentProgressedEvent.getCorrectness();
        CompletedQuizzesGoalProgressEvent completedQuizzesGoalProgressEvent = new CompletedQuizzesGoalProgressEvent();
        completedQuizzesGoalProgressEvent.setProgressType(ProgressType.QUIZ);
        completedQuizzesGoalProgressEvent.setUserId(user.getId());
        completedQuizzesGoalProgressEvent.setCourseId(contendId);
        completedQuizzesGoalProgressEvent.setScore(correctness);
        completedQuizzesGoalProgressEvent.setContentId(contendId);
        user.getUserGoalProgressEntities().forEach(goalProgressEntity -> {
            goalProgressEntity.updateProgress(completedQuizzesGoalProgressEvent);
        });
        userRepository.save(user);
    }

    private void mediaProgress(final ContentProgressedEvent contentProgressedEvent, UserEntity user) {

    }

    public void chapterProgress(final UserProgressUpdatedEvent userProgressUpdatedEvent) {
        UUID courseId = userProgressUpdatedEvent.getCourseId();
        CourseEntity courseEntity = courseRepository.findById(courseId).orElseGet(() -> createCourse(courseId));
        UUID userId = userProgressUpdatedEvent.getUserId();
        UUID chapterId = userProgressUpdatedEvent.getChapterId();
        UserEntity user = userRepository.findById(userId).orElse(generateUser(userId));
        if (!user.getCourseIds().contains(courseEntity.getId())) {
            addUserToCourse(courseEntity, user);
        }
        CompleteSpecificChapterGoalProgressEvent completeSpecificChapterGoalProgressEvent = new CompleteSpecificChapterGoalProgressEvent();
        completeSpecificChapterGoalProgressEvent.setProgressType(ProgressType.CHAPTER);
        completeSpecificChapterGoalProgressEvent.setUserId(userId);
        completeSpecificChapterGoalProgressEvent.setChapterId(chapterId);
        completeSpecificChapterGoalProgressEvent.setCourseId(courseId);
        user.getUserGoalProgressEntities().forEach(goalProgressEntity -> {
            goalProgressEntity.updateProgress(completeSpecificChapterGoalProgressEvent);
        });
        userRepository.save(user);
    }

    public void forumProgress(final ForumActivityEvent forumActivityEvent) {
        UUID courseId = forumActivityEvent.getCourseId();
        CourseEntity courseEntity = courseRepository.findById(courseId).orElseGet(() -> createCourse(courseId));
        log.info(courseEntity.toString());
        UUID userId = forumActivityEvent.getUserId();
        UserEntity user = userRepository.findById(userId).orElse(generateUser(userId));
        if (!user.getCourseIds().contains(courseEntity.getId())) {
            addUserToCourse(courseEntity, user);
        }
        switch (forumActivityEvent.getActivity()) {
            case ANSWER -> forumAnswerProgress(user, courseId);
            case INFO -> forumInfoProgress(user, courseId);
        }
    }

    private void forumAnswerProgress(UserEntity user, UUID courseId) {
        GoalProgressEvent goalProgressEvent = new GoalProgressEvent();
        goalProgressEvent.setUserId(user.getId());
        goalProgressEvent.setCourseId(courseId);
        goalProgressEvent.setProgressType(ProgressType.FORUM);
        user.getUserGoalProgressEntities().forEach(userGoalProgressEntity -> {
            userGoalProgressEntity.updateProgress(goalProgressEvent);
        });
        userRepository.save(user);
    }

    private void forumInfoProgress(UserEntity user, UUID courseId) {}

    public UUID loginUser(UUID userId, UUID courseId) {
        CourseEntity courseEntity = courseRepository.findById(courseId).orElseGet(() -> createCourse(courseId));
        UserEntity user = userRepository.findById(userId).orElseGet(() -> generateUser(userId));
        if (!user.getCourseIds().contains(courseEntity.getId())) {
            addUserToCourse(courseEntity, user);
        }
        LoginStreakGoalProgressEvent loginStreakGoalProgressEvent = new LoginStreakGoalProgressEvent();
        loginStreakGoalProgressEvent.setUserId(userId);
        loginStreakGoalProgressEvent.setCourseId(courseId);
        loginStreakGoalProgressEvent.setProgressType(ProgressType.LOGIN);
        loginStreakGoalProgressEvent.setLoginTime(OffsetDateTime.now());
        user.getUserGoalProgressEntities().forEach(userGoalProgressEntity -> {
            userGoalProgressEntity.updateProgress(loginStreakGoalProgressEvent);
        });
        userRepository.save(user);
        return userId;
    }

    public List<Achievement> getAchievementsForUserInCourse(UUID userId, UUID courseId) {
        CourseEntity courseEntity = courseRepository.findById(courseId).orElseThrow(()
                -> new EntityNotFoundException("Course with the id " + courseId + " not found"));
        UserEntity user = userRepository.findById(userId).orElseThrow(()
                -> new EntityNotFoundException("User with the id " + userId + " not found"));
        List<UserGoalProgressEntity> userGoalProgressEntities = courseEntity.getAchievements().stream()
                .map(achievement -> userGoalProgressRepository.findAllByUserAndGoal(user, achievement.getGoal()))
                .flatMap(List::stream).toList();
        List<Achievement> userAchievements = new ArrayList<>();
        generateAchievements(getUserGoalProgressEntities(userGoalProgressEntities), courseId, userAchievements);
        return userAchievements;
    }

    private static void generateAchievements(List<UserGoalProgressEntity> userGoalProgressEntities, UUID courseId, List<Achievement> userAchievements) {
        userGoalProgressEntities.forEach(userGoalProgressEntity -> {
            Achievement achievement = new Achievement();
            achievement.setId(userGoalProgressEntity.getGoal().getAchievement().getId());
            achievement.setName(userGoalProgressEntity.getGoal().getAchievement().getName());
            achievement.setDescription(userGoalProgressEntity.getGoal().generateDescription());
            achievement.setCourseId(courseId);
            achievement.setImageUrl(userGoalProgressEntity.getGoal().getAchievement().getImageUrl());
            achievement.setTrackingEndTime(userGoalProgressEntity.getEndedAt());
            achievement.setTrackingStartTime(userGoalProgressEntity.getStartedAt());
            achievement.setCompleted(userGoalProgressEntity.isCompleted());
            if (userGoalProgressEntity instanceof CountableUserGoalProgressEntity countableUserGoalProgressEntity) {
                if (countableUserGoalProgressEntity.getGoal() instanceof CountableGoalEntity countableGoalEntity) {
                    achievement.setRequiredCount(countableGoalEntity.getRequiredCount());
                    achievement.setCompletedCount(countableUserGoalProgressEntity.getCompletedCount());
                }
            }
            userAchievements.add(achievement);
        });
    }

    private static List<UserGoalProgressEntity> getUserGoalProgressEntities(List<UserGoalProgressEntity> userGoalProgressEntities) {
        return userGoalProgressEntities;
    }

    public List<Achievement> getAchievementsForUser(UUID userId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(()
                -> new EntityNotFoundException("User with the id " + userId + " not found"));
        log.info("get achievements for user {}", user.getId());
        List<Achievement> userAchievements = new ArrayList<>();
        user.getCourseIds().forEach(courseId -> {
            log.info("courseId {}", courseId);
            CourseEntity courseEntity = courseRepository.findById(courseId).orElse(null);
            log.info("CourseEntity: {}", courseEntity);
            if (courseEntity != null) {
                log.info("Generate achievement for user {} with course {}", userId, courseId);
                List<UserGoalProgressEntity> userGoalProgressEntities = courseEntity.getAchievements().stream()
                        .map(achievement -> userGoalProgressRepository.findAllByUserAndGoal(user, achievement.getGoal()))
                        .flatMap(List::stream).toList();
                generateAchievements(userGoalProgressEntities, courseId, userAchievements);
            }
        });
        return userAchievements;

    }

    private CourseEntity createCourse(final UUID courseId) {
        log.info("try to create course {}", courseId);
        CourseEntity courseEntity = new CourseEntity();
        courseEntity.setId(courseId);
        courseEntity.setChapters(courseServiceClient.queryChapterByCourseId(courseId));
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        achievementRepository.saveAll(achievementEntities);
        courseEntity.setAchievements(achievementEntities);
        courseRepository.save(courseEntity);
        log.info("Created course with id {}", courseId);
        log.info("Created course {}", courseEntity);
        return courseEntity;
    }

    private void addUserToCourse(final CourseEntity course, final UserEntity user) {
        log.info("add user to course {}", course.getId());
        user.getCourseIds().add(course.getId());
        userRepository.saveAndFlush(user);
        List<UserGoalProgressEntity> userGoalProgress = course.getAchievements().stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(user)).collect(Collectors.toCollection(ArrayList::new));
        user.setUserGoalProgressEntities(userGoalProgress);
        userRepository.save(user);
        log.info("Added user to course {}", user);
    }

    private UserEntity generateUser(final UUID userId) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setCourseIds(new ArrayList<>());
        userRepository.save(userEntity);
        log.info("Created user with id {}", userId);
        log.info("Created user {}", userEntity);
        return userEntity;
    }
}
