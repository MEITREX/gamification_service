package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.achievements.Achievements;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.CountableGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.CountableUserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.AchievementRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.UserRepository;
import de.unistuttgart.iste.meitrex.generated.dto.Achievement;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AchievementService {
    private final UserRepository userRepository;
    private final AchievementRepository achievementRepository;

    private final Achievements achievements = new Achievements();

    public List<Achievement> getAchievementsForUserInCourse(UUID userId, UUID courseId) {
        Optional<UserEntity> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return new  ArrayList<>();
        }
        List<Achievement> userAchievements = new ArrayList<>();
        List<UserGoalProgressEntity> userGoalProgressEntities = user.get().getUserGoalProgressEntities().stream()
                .filter(userGoalProgressEntity -> {
                            if (userGoalProgressEntity.getGoal().getParentWithGoal() instanceof AchievementEntity achievement) {
                                return  achievement.getCourse().getId().equals(courseId);
                            } else {
                                return false;
                            }
                        }).toList();
        mapUserGoalProgressToAchievements(userGoalProgressEntities, userAchievements);
        return userAchievements;
    }

    private static void mapUserGoalProgressToAchievements(List<UserGoalProgressEntity> userGoalProgressEntities,
                                                          List<Achievement> userAchievements) {
        userGoalProgressEntities.forEach(userGoalProgressEntity -> {
            if (userGoalProgressEntity.getGoal().getParentWithGoal() instanceof AchievementEntity achievementEntity) {
                Achievement achievement = new Achievement();
                achievement.setId(userGoalProgressEntity.getGoal().getParentWithGoal().getId());
                achievement.setName(achievementEntity.getName());
                achievement.setDescription(userGoalProgressEntity.getGoal().generateDescription());
                achievement.setCourseId(achievementEntity.getCourse().getId());
                achievement.setImageUrl(achievementEntity.getImageUrl());
                achievement.setTrackingEndTime(userGoalProgressEntity.getEndedAt());
                achievement.setTrackingStartTime(userGoalProgressEntity.getStartedAt());
                achievement.setCompleted(userGoalProgressEntity.isCompleted());
                if (userGoalProgressEntity instanceof CountableUserGoalProgressEntity countableUserGoalProgressEntity) {
                    if (countableUserGoalProgressEntity.getGoal() instanceof CountableGoalEntity countableGoalEntity) {
                        achievement.setRequiredCount(countableGoalEntity.getRequiredCount());
                        achievement.setCompletedCount(countableUserGoalProgressEntity.getCompletedCount());
                    } else {
                        throw new RuntimeException("UserGoalProgress was countable, but its parent GoalEntity was not" +
                                " countable. This should never happen!");
                    }
                }
                userAchievements.add(achievement);
            }
        });
    }

    public List<Achievement> getAchievementsForUser(UUID userId) {
        Optional<UserEntity> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return new ArrayList<>();
        }
        log.info("get achievements for user {}", user.get().getId());
        List<Achievement> userAchievements = new ArrayList<>();
        mapUserGoalProgressToAchievements(user.get().getUserGoalProgressEntities(), userAchievements);
        return userAchievements;
    }

    public void tryGenerateAdaptiveAchievementForUser(UserEntity user,
                                                      CourseEntity course,
                                                      AchievementEntity completedAchievement) {
        if(!(completedAchievement.getGoal() instanceof CountableGoalEntity countableGoal))
            return;

        AchievementEntity newAchievement = new AchievementEntity();
    }

    public void createInitialAchievementsInCourseEntity(CourseEntity course) {
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(course);
        achievementRepository.saveAll(achievementEntities);
        course.setAchievements(achievementEntities);
    }
}
