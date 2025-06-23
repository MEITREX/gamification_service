package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.common.event.ContentProgressedEvent;
import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.gamification_service.achievements.Achievements;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.*;
import de.unistuttgart.iste.meitrex.generated.dto.Content;
import de.unistuttgart.iste.meitrex.generated.dto.UserGoalProgress;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
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
        Content content = contentServiceClient.queryContentsByIds(contentProgressedEvent.getUserId(),
                List.of(contentProgressedEvent.getContentId())).getFirst();
        UUID courseId = content.getMetadata().getCourseId();
        CourseEntity courseEntity = courseRepository.findById(courseId).orElseGet(() -> createCourse(courseId));
        UUID userId = content.getUserProgressData().getUserId();
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
            userGoalProgressRepository.save(userGoalProgressEntity);
        });
        userRepository.save(user);
    }

    private void mediaProgress(final ContentProgressedEvent contentProgressedEvent, UserEntity user) {

    }

    public List<UserGoalProgress> getAchievementsForUser(UUID userId, UUID courseId) {
        CourseEntity courseEntity = courseRepository.findById(courseId).orElseThrow(()
                -> new EntityNotFoundException("Course with the id " + courseId + " not found"));
        UserEntity user = userRepository.findById(userId).orElseThrow(()
                -> new EntityNotFoundException("User with the id " + userId + " not found"));
        List<UserGoalProgressEntity> userGoalProgressEntities = courseEntity.getAchievements().stream()
                .map(achievement -> userGoalProgressRepository.findAllByUserAndGoal(user, achievement.getGoal()))
                .flatMap(List::stream).toList();
        return userGoalProgressEntities.stream().map(userGoalProgressEntity
                -> modelMapper.map(userGoalProgressEntity, UserGoalProgress.class)).toList();
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
        return userId;
    }

    private CourseEntity createCourse(final UUID courseId) {
        CourseEntity courseEntity = new CourseEntity();
        courseEntity.setId(courseId);
        courseEntity.setNumberOfChapters(12); //TODO get number of Chapters from CourseService
        courseRepository.save(courseEntity);
        achievements.generateAchievements(courseEntity, achievementRepository, goalRepository);
        courseRepository.save(courseEntity);
        log.info("Created course with id {}", courseId);
        return courseEntity;
    }

    private UserEntity generateUser(final UUID userId, List<AchievementEntity> achievements) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userRepository.save(userEntity);
        List<UserGoalProgressEntity> userGoalProgress = achievements.stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity, achievement.getGoal())).toList();
        userGoalProgressRepository.saveAll(userGoalProgress);
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        userRepository.save(userEntity);
        log.info("Created user with id {}", userId);
        return userEntity;
    }
}
