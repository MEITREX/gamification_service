package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.exception.ResourceNotFoundException;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserCourseDataEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.HasGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.CountableGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.CountableUserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserRepository;
import de.unistuttgart.iste.meitrex.generated.dto.Achievement;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DefaultAchievementService implements IAchievementService {

    // Static Business Logic

    /**
     * Maps "UserGoalProgressEntity"s (database types) to "Achievement"s (DTO types). This method also ensures that
     * only UserGoalProgressEntities that are actually achievements are mapped. Non-achievements are skipped.
     *
     * @param userGoalProgressEntities the list of UserGoalProgressEntities to map. Can contain any type of goal
     *                                 progress, but only those that are achievements will be mapped to achievements.
     * @param userAchievements         the list of achievements to which the mapped achievements will be added.
     *
     */
    private static void mapUserGoalProgressToAchievements(List<UserGoalProgressEntity> userGoalProgressEntities, List<Achievement> userAchievements) {
        userGoalProgressEntities.forEach(userGoalProgressEntity -> {
            if (Hibernate.unproxy(userGoalProgressEntity.getGoal().getParentWithGoal(), HasGoalEntity.class)
                    instanceof AchievementEntity achievementEntity) {
                Achievement achievement = new Achievement();
                achievement.setId(userGoalProgressEntity.getGoal().getParentWithGoal().getId());
                achievement.setName(achievementEntity.getName());
                achievement.setDescription(userGoalProgressEntity.getGoal().generateDescription());
                achievement.setCourseId(achievementEntity.getCourse().getId());
                achievement.setAdaptive(achievementEntity.isAdaptive());
                achievement.setImageUrl(achievementEntity.getImageUrl());
                achievement.setTrackingEndTime(userGoalProgressEntity.getEndedAt());
                achievement.setTrackingStartTime(userGoalProgressEntity.getStartedAt());
                achievement.setCompleted(userGoalProgressEntity.isCompleted());
                achievement.setUserId(userGoalProgressEntity.getUser().getId());
                if (userGoalProgressEntity instanceof CountableUserGoalProgressEntity countableUserGoalProgressEntity) {
                    GoalEntity goal = (GoalEntity) Hibernate.unproxy(countableUserGoalProgressEntity.getGoal());
                    if (goal instanceof CountableGoalEntity countableGoalEntity) {
                        achievement.setRequiredCount(countableGoalEntity.getRequiredCount());
                        achievement.setCompletedCount(countableUserGoalProgressEntity.getCompletedCount());
                    } else {
                        throw new RuntimeException("UserGoalProgress was countable, but its parent GoalEntity was not" +
                                " countable. This should never happen! instaceof goal: " + userGoalProgressEntity.getGoal().getClass().getName());
                    }
                }
                userAchievements.add(achievement);
            }
        });
    }

    /**
     * Gets all achievements of a UserEntity, including adaptive achievements, as DTOs.
     *
     * @param user The UserEntity to get achievements for.
     * @return A list of Achievement DTOs representing the user's achievements.
     */
    public static List<Achievement> getAchievementsForUserEntity(UserEntity user) {
        log.info("Get achievements for user {}", user.getId());
        List<UserGoalProgressEntity> allUserGoalProgress = user
                .getCourseData()
                .stream()
                .flatMap(cd -> cd.getGoalProgressEntities().stream())
                .toList();
        List<Achievement> userAchievements = new ArrayList<>();
        mapUserGoalProgressToAchievements(allUserGoalProgress, userAchievements);
        return userAchievements;
    }


    // Attributes

    private IUserRepository userRepository;

    public DefaultAchievementService(@Autowired IUserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }


    @Override
    public List<Achievement> getAchievementsForUserInCourse(UUID userId, UUID courseId) {
        List<Achievement> resultList = new ArrayList<>();
        final Optional<UserEntity> userEntityOptional = this.userRepository.findById(userId);
        if(userEntityOptional.isEmpty()) {
            return resultList;
        }
        final UserEntity userEntity = userEntityOptional.get();
        final Optional<UserCourseDataEntity> userCourseData = userEntity.getCourseData(courseId);
        if(userCourseData.isEmpty()) {
            return resultList;
        }
        final UserCourseDataEntity userCourseDataEntity = userCourseData.get();
        mapUserGoalProgressToAchievements(userCourseDataEntity.getGoalProgressEntities(), resultList);
        return resultList;
    }


    @Override
    public List<Achievement> getAchievementsForUser(UUID userId) {
        Optional<UserEntity> userOptional = userRepository.findById(userId);
        return userOptional
                .map(DefaultAchievementService::getAchievementsForUserEntity)
                .orElseGet(ArrayList::new);
    }

}
