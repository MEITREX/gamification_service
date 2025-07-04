package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.common.event.ContentProgressedEvent;
import de.unistuttgart.iste.meitrex.common.event.ForumActivityEvent;
import de.unistuttgart.iste.meitrex.common.event.UserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.course_service.client.CourseServiceClient;
import de.unistuttgart.iste.meitrex.gamification_service.achievements.Achievements;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.*;
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

import static graphql.scalars.ExtendedScalars.DateTime;

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
    public void progessUserProgress(final ContentProgressedEvent contentProgressedEvent)
            throws ContentServiceConnectionException {
        UUID userId = contentProgressedEvent.getUserId();
        Content content = contentServiceClient.queryContentsByIds(userId,
                List.of(contentProgressedEvent.getContentId())).getFirst();
        UUID courseId = content.getMetadata().getCourseId();
        CourseEntity courseEntity = courseRepository.findById(courseId).orElseGet(() -> createCourse(courseId));
        UserEntity user = userRepository.findById(userId).orElse(generateUser(userId, courseEntity.getAchievements()));
        switch (content.getMetadata().getType()) {
            case QUIZ -> quizProgress(contentProgressedEvent, user);
            case MEDIA -> mediaProgress(contentProgressedEvent, user);
        }
    }

    private void quizProgress(final ContentProgressedEvent contentProgressedEvent, UserEntity user) {
        log.info("Quiz progress");
        List<CountableUserGoalProgressEntity> userGoalProgressEntities = user.getUserGoalProgressEntities().stream()
                .filter(userGoalProgressEntity -> userGoalProgressEntity.getGoal() instanceof CompletedQuizzesGoalEntity)
                .filter(userGoalProgressEntity -> userGoalProgressEntity instanceof CountableUserGoalProgressEntity)
                .map(userGoalProgressEntity -> (CountableUserGoalProgressEntity) userGoalProgressEntity).toList();
        userGoalProgressEntities.forEach(userGoalProgressEntity ->{
            CompletedQuizzesGoalEntity goalEntity = (CompletedQuizzesGoalEntity) userGoalProgressEntity.getGoal();
            goalEntity.updateProgress(userGoalProgressEntity, (float) contentProgressedEvent.getCorrectness(),
                    contentProgressedEvent.getContentId());
        });
        userRepository.save(user);
    }

    private void mediaProgress(final ContentProgressedEvent contentProgressedEvent, UserEntity user) {

    }

    public void chapterProgress(final UserProgressUpdatedEvent userProgressUpdatedEvent) {
        UUID courseId = userProgressUpdatedEvent.getCourseId();
        CourseEntity courseEntity = courseRepository.findById(courseId).orElseGet(() -> createCourse(courseId));
        UUID userId = userProgressUpdatedEvent.getUserId();
        UserEntity user = userRepository.findById(userId).orElse(generateUser(userId, courseEntity.getAchievements()));
        List<UserGoalProgressEntity> userGoalProgressEntities = user.getUserGoalProgressEntities().stream()
                .filter(userGoalProgressEntity ->
                        userGoalProgressEntity.getGoal() instanceof CompleteSpecificChapterGoalEntity).toList();
        userGoalProgressEntities.forEach(userGoalProgressEntity -> {
            CompleteSpecificChapterGoalEntity goalEntity = (CompleteSpecificChapterGoalEntity) userGoalProgressEntity.getGoal();
            goalEntity.updateProgress(userGoalProgressEntity);
        });
        userRepository.save(user);
    }

    public void forumProgress(final ForumActivityEvent forumActivityEvent) {
        UUID courseId = forumActivityEvent.getCourseId();
        CourseEntity courseEntity = courseRepository.findById(courseId).orElseGet(() -> createCourse(courseId));
        UUID userId = forumActivityEvent.getUserId();
        UserEntity user = userRepository.findById(userId).orElse(generateUser(userId, courseEntity.getAchievements()));
        switch (forumActivityEvent.getActivity()) {
            case ANSWER -> forumAnswerProgress(user);
            case INFO -> forumInfoProgress(user);
        }
    }

    private void forumAnswerProgress(UserEntity user) {
        List<CountableUserGoalProgressEntity> userGoalProgressEntities = user.getUserGoalProgressEntities().stream()
                .filter(userGoalProgressEntity -> userGoalProgressEntity.getGoal() instanceof AnswerForumQuestionGoalEntity)
                .map(userGoalProgressEntity -> (CountableUserGoalProgressEntity) userGoalProgressEntity).toList();
        userGoalProgressEntities.forEach(userGoalProgressEntity -> {
            AnswerForumQuestionGoalEntity goalEntity = (AnswerForumQuestionGoalEntity) userGoalProgressEntity.getGoal();
            goalEntity.updateProgress(userGoalProgressEntity);
            userGoalProgressRepository.save(userGoalProgressEntity);
        });
        userRepository.save(user);
    }

    private void forumInfoProgress(UserEntity user) {}

    public List<Achievement> getAchievementsForUser(UUID userId, UUID courseId) {
        CourseEntity courseEntity = courseRepository.findById(courseId).orElseThrow(()
                -> new EntityNotFoundException("Course with the id " + courseId + " not found"));
        UserEntity user = userRepository.findById(userId).orElseThrow(()
                -> new EntityNotFoundException("User with the id " + userId + " not found"));
        List<UserGoalProgressEntity> userGoalProgressEntities = courseEntity.getAchievements().stream()
                .map(achievement -> userGoalProgressRepository.findAllByUserAndGoal(user, achievement.getGoal()))
                .flatMap(List::stream).toList();
        List<Achievement> userAchievements = new ArrayList<>();
        userGoalProgressEntities.forEach(userGoalProgressEntity -> {
            Achievement achievement = new Achievement();
            achievement.setId(userGoalProgressEntity.getGoal().getAchievement().getId());
            achievement.setName(userGoalProgressEntity.getGoal().getAchievement().getName());
            achievement.setDescription(userGoalProgressEntity.getGoal().generateDescription());
            achievement.setCourseId(courseId);
            achievement.setImageUrl(userGoalProgressEntity.getGoal().getAchievement().getImageUrl());
            achievement.setTrackingEndTime(userGoalProgressEntity.getGoal().getTrackingEndTime());
            achievement.setTrackingStartTime(userGoalProgressEntity.getGoal().getTrackingStartTime());
            achievement.setCompleted(userGoalProgressEntity.isCompleted());
            if (userGoalProgressEntity instanceof CountableUserGoalProgressEntity countableUserGoalProgressEntity) {
                if (countableUserGoalProgressEntity.getGoal() instanceof CountableGoalEntity countableGoalEntity) {
                    achievement.setRequiredCount(countableGoalEntity.getRequiredCount());
                    achievement.setCompletedCount(countableUserGoalProgressEntity.getCompletedCount());
                }
            }
            userAchievements.add(achievement);
        });


        return userAchievements;

    }

    public UUID loginUser(UUID userId, UUID courseId) {
        CourseEntity course = courseRepository.findById(courseId).orElseGet(() -> createCourse(courseId));
        UserEntity user = userRepository.findById(userId).orElseGet(() -> generateUser(userId, course.getAchievements()));
        user.getUserGoalProgressEntities().stream()
                .filter(userGoalProgressEntity -> userGoalProgressEntity.getGoal() instanceof LoginStreakGoalEntity)
                .filter(userGoalProgressEntity -> userGoalProgressEntity instanceof CountableUserGoalProgressEntity)
                .map(userGoalProgressEntity -> (CountableUserGoalProgressEntity) userGoalProgressEntity)
                .forEach(userGoalProgressEntity -> {
                    LoginStreakGoalEntity goalEntity = (LoginStreakGoalEntity) userGoalProgressEntity.getGoal();
                    goalEntity.updateProgress(userGoalProgressEntity, OffsetDateTime.now());
                });
        userRepository.save(user);
        return userId;
    }

    private CourseEntity createCourse(final UUID courseId) {
        CourseEntity courseEntity = new CourseEntity();
        courseEntity.setId(courseId);
        courseEntity.setChapters(courseServiceClient.queryChapterByCourseId(courseId));
        achievements.generateAchievements(courseEntity);
        courseRepository.save(courseEntity);
        log.info("Created course with id {}", courseId);
        return courseEntity;
    }

    private UserEntity generateUser(final UUID userId, List<AchievementEntity> achievementEntities) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        List<UserGoalProgressEntity> userGoalProgress = achievementEntities.stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity, achievement.getGoal())).toList();
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        userRepository.save(userEntity);
        log.info("Created user with id {}", userId);
        return userEntity;
    }
}
