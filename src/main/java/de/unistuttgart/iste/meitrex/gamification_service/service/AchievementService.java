package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.achievements.Achievements;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.CountableGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.CountableUserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.AchievementRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.UserGoalProgressRepository;
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
    private final UserGoalProgressRepository userGoalProgressRepository;

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

    public void tryGenerateAdaptiveAchievementForUser(UserGoalProgressEntity goalProgress,
                                                      CourseEntity course,
                                                      AchievementEntity completedAchievement) {
        // we can only create adaptive achievements based on countable achievements (e.g. "complete 5 quizzes")
        if(!(completedAchievement.getGoal() instanceof CountableGoalEntity countableGoal))
            return;

        if(!(goalProgress instanceof CountableUserGoalProgressEntity countableGoalProgress))
            throw new RuntimeException("Goal was countable but user goal progress was not. This should never happen!");

        UserEntity user = goalProgress.getUser();

        // TODO: Only generate new achievement for user if they haven't reached the max num of adaptive achievements yet

        // clone goal of original achievement but increase the number of required completions
        GoalEntity newGoal = completedAchievement.getGoal().clone();
        if(!(newGoal instanceof CountableGoalEntity newGoalCountable))
            throw new RuntimeException("Cloned goal was not countable even though base goal was countable. This" +
                    " should never happen!");
        newGoalCountable.setRequiredCount(countableGoal.getRequiredCount() * 3);

        // search for an existing achievement with the same goal (maybe another user has already generated this
        // adaptive achievement, we can reuse it)
        Optional<AchievementEntity> otherUsersAchievement =
                achievementRepository.getAchievementEntitiesByCourse(course).stream()
                .filter(a -> a.getGoal().equalsGoalTargets(newGoalCountable))
                .findAny();

        // create the new adaptive achievement if no user has reached it before or reuse another user's
        AchievementEntity newAchievement;
        if (otherUsersAchievement.isPresent()) {
            newAchievement = otherUsersAchievement.get();
        } else {
            newAchievement = new AchievementEntity();
            newAchievement.setName(completedAchievement.getName() + "I");
            newAchievement.setImageUrl("");
            newAchievement.setGoal(newGoalCountable);

            // TODO: Store achievement in achievement repository?
        }

        // create a user goal progress for our user
        UserGoalProgressEntity newGoalProgress = newAchievement.getGoal().generateUserGoalProgress(user);

        // do not generate a new goal progress if user already has a goal progress for this achievement
        if(!userGoalProgressRepository.findAllByUserAndGoal(user, newAchievement.getGoal()).isEmpty())
            return;

        if(!(newGoalProgress instanceof CountableUserGoalProgressEntity newGoalProgressCountable))
            throw new RuntimeException("Goal is countable but created goal progress is not. This should never happen!");

        // transfer num of completions from goal progress of old achievement to goal progress of new achievement
        // (we do this because achievements are meant to track your all-time progress, not only a specific time period,
        // so we also need the progress to include events from before this new achievement was created)
        newGoalProgressCountable.setCompletedCount(countableGoalProgress.getCompletedCount());

        userGoalProgressRepository.save(newGoalProgress);
    }

    public void createInitialAchievementsInCourseEntity(CourseEntity course) {
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(course);
        achievementRepository.saveAll(achievementEntities);
        course.setAchievements(achievementEntities);
    }
}
