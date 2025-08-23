package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.common.dapr.TopicPublisher;
import de.unistuttgart.iste.meitrex.common.event.AchievementCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.achievements.Achievements;
import de.unistuttgart.iste.meitrex.gamification_service.config.AdaptivityConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserCourseDataEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.HasGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.CountableGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.CountableUserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.AchievementRepository;
import de.unistuttgart.iste.meitrex.generated.dto.Achievement;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AchievementService {
    private final UserService userService;
    private final AchievementRepository achievementRepository;
    private final AdaptivityConfiguration adaptivityConfiguration;
    private final TopicPublisher topicPublisher;

    private final Achievements achievements = new Achievements();

    /**
     * Returns the achievements of the user with the given userId in the course with the given courseId.
     *
     * @param userId   ID of the user to get achievements for.
     * @param courseId ID of the course to get achievements for.
     * @return A list of Achievement DTOs representing the user's achievements in the specified course.
     */
    public List<Achievement> getAchievementsForUserInCourse(UUID userId, UUID courseId) {
        Optional<UserEntity> user = userService.getUser(userId);
        if (user.isEmpty())
            return Collections.emptyList();

        Optional<UserCourseDataEntity> userCourseData = user.get().getCourseData(courseId);
        if (userCourseData.isEmpty())
            return Collections.emptyList();

        List<Achievement> userAchievements = new ArrayList<>();
        mapUserGoalProgressToAchievements(userCourseData.get().getGoalProgressEntities(), userAchievements);
        return userAchievements;
    }

    /**
     * Maps "UserGoalProgressEntity"s (database types) to "Achievement"s (DTO types). This method also ensures that
     * only UserGoalProgressEntities that are actually achievements are mapped. Non-achievements are skipped.
     *
     * @param userGoalProgressEntities the list of UserGoalProgressEntities to map. Can contain any type of goal
     *                                 progress, but only those that are achievements will be mapped to achievements.
     * @param userAchievements         the list of achievements to which the mapped achievements will be added.
     */
    protected static void mapUserGoalProgressToAchievements(List<UserGoalProgressEntity> userGoalProgressEntities,
                                                            List<Achievement> userAchievements) {
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
     * Gets all achievements of a user, including adaptive achievements, as DTOs.
     *
     * @param userId ID of the user to get achievements for.
     * @return A list of Achievement DTOs representing the user's achievements.
     */
    public List<Achievement> getAchievementsForUser(UUID userId) {
        Optional<UserEntity> user = userService.getUser(userId);
        if (user.isEmpty()) {
            return Collections.emptyList();
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

        List<UserGoalProgressEntity> allUserGoalProgress = user.getCourseData().stream()
                .flatMap(cd -> cd.getGoalProgressEntities().stream())
                .toList();

        List<Achievement> userAchievements = new ArrayList<>();
        mapUserGoalProgressToAchievements(allUserGoalProgress, userAchievements);
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
        if (!(Hibernate.unproxy(completedAchievement.getGoal(), GoalEntity.class)
                instanceof CountableGoalEntity countableGoal))
            return;

        if (!(Hibernate.unproxy(goalProgress, UserGoalProgressEntity.class)
                instanceof CountableUserGoalProgressEntity countableGoalProgress))
            throw new RuntimeException("Goal was countable but user goal progress was not. This should never happen!");

        final UserEntity user = goalProgress.getUser();

        final UserCourseDataEntity userCourseData = user.getCourseData(course.getId())
                .orElseThrow(() -> new RuntimeException("User course data could not be found for course user is a" +
                        " member of. This should never happen! User: " + user.getId() + " Course: " + course.getId()));

        // Only generate new achievement for user if they haven't reached the max num of adaptive achievements yet
        if (hasUserMaxAdaptiveAchievements(user))
            return;

        // clone goal of original achievement but increase the number of required completions
        final GoalEntity newGoal = completedAchievement.getGoal().clone();
        if (!(newGoal instanceof CountableGoalEntity newGoalCountable))
            throw new RuntimeException("Cloned goal was not countable even though base goal was countable. This" +
                    " should never happen!");
        newGoalCountable.setRequiredCount(countableGoal.getRequiredCount() * 3);

        // search for an existing achievement with the same goal (maybe another user has already generated this
        // adaptive achievement, we can reuse it)
        final Optional<AchievementEntity> otherUsersAchievement =
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
            if (completedAchievement.isAdaptive()) {
                String[] nameParts = completedAchievement.getName().split(" ");
                int lastLevel = Integer.parseInt(nameParts[nameParts.length - 1]);
                String baseName = String.join(" ", Arrays.copyOf(nameParts, nameParts.length - 1));
                newAchievement.setName(baseName + " " + (lastLevel + 1));
            } else {
                newAchievement.setName(completedAchievement.getName() + " 2");
            }
            newAchievement.setImageUrl("");
            newAchievement.setGoal(newGoalCountable);

            achievementRepository.save(newAchievement);
        }

        // create a user goal progress for our user
        final UserGoalProgressEntity newGoalProgress = newAchievement.getGoal().generateUserGoalProgress(user);

        // do not generate a new goal progress if user already has an equal goal progress for this achievement
        if (userCourseData.getGoalProgressEntities().stream()
                .anyMatch(gp -> gp.getGoal().getId().equals(newGoalProgress.getId())))
            return;

        if (!(newGoalProgress instanceof CountableUserGoalProgressEntity newGoalProgressCountable))
            throw new RuntimeException("Goal is countable but created goal progress is not. This should never happen!");

        // transfer num of completions from goal progress of old achievement to goal progress of new achievement
        // (we do this because achievements are meant to track your all-time progress, not only a specific time period,
        // so we also need the progress to include events from before this new achievement was created)
        newGoalProgressCountable.setCompletedCount(countableGoalProgress.getCompletedCount());

        userCourseData.getGoalProgressEntities().add(newGoalProgress);
        userService.upsertUser(user);
    }

    public void onAchievementCompleted(AchievementEntity achievement, UserGoalProgressEntity goalProgressEntity) {
        AchievementCompletedEvent event = AchievementCompletedEvent.builder()
                .userId(goalProgressEntity.getUser().getId())
                .achievementId(achievement.getId())
                .courseId(achievement.getCourse().getId())
                .build();
        topicPublisher.notifyAchievementCompleted(event);

        tryGenerateAdaptiveAchievementForUser(
                goalProgressEntity,
                achievement.getCourse(),
                achievement);
    }

    /**
     * Creates the initial achievements for a course entity and stores them in the course entity.
     *
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
