package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.achievements.Achievements;
import de.unistuttgart.iste.meitrex.gamification_service.config.AdaptivityConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
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
import org.hibernate.Hibernate;
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
    private final AdaptivityConfiguration adaptivityConfiguration;

    private final Achievements achievements = new Achievements();

    /**
     * Returns the achievements of the user with the given userId in the course with the given courseId.
     * @param userId ID of the user to get achievements for.
     * @param courseId ID of the course to get achievements for.
     * @return A list of Achievement DTOs representing the user's achievements in the specified course.
     */
    public List<Achievement> getAchievementsForUserInCourse(UUID userId, UUID courseId) {
        return getAchievementsForUser(userId).stream()
                .filter(achievement -> achievement.getCourseId().equals(courseId))
                .toList();
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
                achievement.setAdaptive(achievementEntity.isAdaptive());
                achievement.setImageUrl(achievementEntity.getImageUrl());
                achievement.setTrackingEndTime(userGoalProgressEntity.getEndedAt());
                achievement.setTrackingStartTime(userGoalProgressEntity.getStartedAt());
                achievement.setCompleted(userGoalProgressEntity.isCompleted());
                if (userGoalProgressEntity instanceof CountableUserGoalProgressEntity countableUserGoalProgressEntity) {
                    GoalEntity goal = (GoalEntity)Hibernate.unproxy(countableUserGoalProgressEntity.getGoal());
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
     * Gets all achievements of a user, including adaptive achievements, as DTOs.
     *
     * @param userId ID of the user to get achievements for.
     * @return A list of Achievement DTOs representing the user's achievements.
     */
    public List<Achievement> getAchievementsForUser(UUID userId) {
        Optional<UserEntity> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            return new ArrayList<>();
        }
        return getAchievementsForUserEntity(user.get());
    }

    /**
     * Gets all achievements of a UserEntity, including adaptive achievements, as DTOs.
     *
     * @param user The UserEntity to get achievements for.
     * @return A list of Achievement DTOs representing the user's achievements.
     */
    private List<Achievement> getAchievementsForUserEntity(UserEntity user) {
        log.info("get achievements for user {}", user.getId());
        List<Achievement> userAchievements = new ArrayList<>();
        mapUserGoalProgressToAchievements(user.getUserGoalProgressEntities(), userAchievements);
        return userAchievements;
    }

    /**
     * Call this method with a user goal progress entity and the AchievementEntity of a completed achievement to
     * generate an adaptive achievement for the user in the course specified.
     */
    public void tryGenerateAdaptiveAchievementForUser(UserGoalProgressEntity goalProgress,
                                                      CourseEntity course,
                                                      AchievementEntity completedAchievement) {
        // we can only create adaptive achievements based on countable achievements (e.g. "complete 5 quizzes")
        if(!(Hibernate.unproxy(completedAchievement.getGoal(), GoalEntity.class)
                instanceof CountableGoalEntity countableGoal))
            return;

        if(!(Hibernate.unproxy(goalProgress, UserGoalProgressEntity.class)
                instanceof CountableUserGoalProgressEntity countableGoalProgress))
            throw new RuntimeException("Goal was countable but user goal progress was not. This should never happen!");

        UserEntity user = goalProgress.getUser();

        // Only generate new achievement for user if they haven't reached the max num of adaptive achievements yet
        if(hasUserMaxAdaptiveAchievements(user))
            return;

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
            newAchievement.setCourse(course);
            newAchievement.setAdaptive(true);

            // TODO: This name generation is a placeholder, we just increase a number for higher-difficulty achievements
            if(completedAchievement.isAdaptive()) {
                String[] nameParts = completedAchievement.getName().split(" ");
                int lastLevel = Integer.parseInt(nameParts[1]);
                newAchievement.setName(nameParts[0] + " " + (lastLevel + 1));
            } else {
                newAchievement.setName(completedAchievement.getName() + " 2");
            }
            newAchievement.setImageUrl("");
            newAchievement.setGoal(newGoalCountable);

            achievementRepository.save(newAchievement);
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

        newGoalProgress = userGoalProgressRepository.save(newGoalProgress);
        user.getUserGoalProgressEntities().add(newGoalProgress);
        userRepository.save(user);
    }

    /**
     * Creates the initial achievements for a course entity and stores them in the course entity.
     * @param course The course entity to create initial achievements in.
     */
    public void createInitialAchievementsInCourseEntity(CourseEntity course) {
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(course);
        achievementEntities = achievementRepository.saveAll(achievementEntities);
        course.setAchievements(achievementEntities);
    }

    /**
     * Helper method to check if a user has reached the maximum number of adaptive achievements.
     */
    private boolean hasUserMaxAdaptiveAchievements(UserEntity user) {
        long userAdaptiveAchievementCount = getAchievementsForUserEntity(user).stream()
                .filter(Achievement::getAdaptive)
                .count();
        return userAdaptiveAchievementCount >= adaptivityConfiguration.getMaxAdaptiveAchievementCount();
    }
}
