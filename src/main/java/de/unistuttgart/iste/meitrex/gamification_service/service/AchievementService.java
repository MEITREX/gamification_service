package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.common.event.ContentProgressedEvent;
import de.unistuttgart.iste.meitrex.common.event.ItemResponse;
import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.content_service.persistence.entity.ContentEntity;
import de.unistuttgart.iste.meitrex.gamification_service.achievements.Achievements;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.*;
import de.unistuttgart.iste.meitrex.generated.dto.Achievement;
import de.unistuttgart.iste.meitrex.generated.dto.Content;
import de.unistuttgart.iste.meitrex.generated.dto.UserGoalProgress;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    private final CourseRepository courseRepository;
    private final CompletedQuizzesGoalRepository completedQuizzesGoalRepository;
    private final Achievements achievements = new Achievements();
    private final UserRepository userRepository;
    private final UserGoalProgressRepository userGoalProgressRepository;
    private final ModelMapper modelMapper;

    /**
     * Logs user progress according to the given event.
     * The learning interval of the user progress data entity will be updated.
     * A new progress log item will be added to the progress log.
     * The event will be forwarded to the topic "user-progress-updated".
     *
     * @param contentProgressedEvent the event to log
     */
    public void logUserProgress(final ContentProgressedEvent contentProgressedEvent)
            throws ContentServiceConnectionException {
        Content content = contentServiceClient.queryContentsByIds(contentProgressedEvent.getUserId(),
                List.of(contentProgressedEvent.getContentId())).getFirst();
        log.info(content.toString());
        UUID courseId = content.getMetadata().getCourseId();
        CourseEntity courseEntity = courseRepository.findById(courseId).orElseGet(() -> createCourse(courseId));
        UUID userId = content.getUserProgressData().getUserId();
        UserEntity user = userRepository.findById(userId).orElse(generateUser(userId, courseEntity.getAchievements()));
        user.getUserGoalProgressEntities().stream().filter(userGoalProgressEntity ->
                userGoalProgressEntity instanceof CountableUserGoalProgressEntity)
                .filter(countableUserGoalEntity -> countableUserGoalEntity.getGoal() instanceof CompletedQuizzesGoalEntity)
                .forEach(countableUserGoalEntity -> {
                    ((CompletedQuizzesGoalEntity) countableUserGoalEntity.getGoal()).updateProgress(countableUserGoalEntity,
                            (float) contentProgressedEvent.getCorrectness(), contentProgressedEvent.getContentId());
                });
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

    private CourseEntity createCourse(final UUID courseId) {
        CourseEntity courseEntity = new CourseEntity();
        courseEntity.setId(courseId);
        courseEntity.setNumberOfChapters(12); //TODO get number of Chapters from CourseService
        achievements.generateAchievements(courseEntity);
        courseRepository.save(courseEntity);
        return courseEntity;
    }

    private UserEntity generateUser(final UUID userId, List<AchievementEntity> achievements) {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        List<UserGoalProgressEntity> userGoalProgress = achievements.stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress()).flatMap(List::stream).toList();
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        userRepository.save(userEntity);
        return userEntity;
    }
}
