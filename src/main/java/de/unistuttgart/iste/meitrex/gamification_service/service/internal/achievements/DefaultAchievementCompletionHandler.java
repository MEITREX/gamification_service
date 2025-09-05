package de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements;

import de.unistuttgart.iste.meitrex.common.dapr.TopicPublisher;
import de.unistuttgart.iste.meitrex.common.event.AchievementCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.config.AdaptivityConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserCourseDataEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.CountableGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.CountableUserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.AchievementRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.DefaultAchievementService;
import de.unistuttgart.iste.meitrex.generated.dto.Achievement;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Component
class DefaultAchievementCompletionHandler implements IAchievementCompletionHandler {

    @Value("${app.achievements.completion-reward}")
    private int achievementCompletionReward;

    // Dependencies

    private final TopicPublisher topicPublisher;

    private final ApplicationEventPublisher appPublisher;

    private final AchievementRepository achievementRepository;

    private final AdaptivityConfiguration adaptivityConfiguration;

    // Constructors

    public DefaultAchievementCompletionHandler(@Autowired ApplicationEventPublisher publisher, @Autowired AchievementRepository achievementRepository, @Autowired TopicPublisher topicPublisher, @Autowired AdaptivityConfiguration adaptivityConfiguration) {
        this.appPublisher = Objects.requireNonNull(publisher);
        this.topicPublisher = Objects.requireNonNull(topicPublisher);
        this.achievementRepository = Objects.requireNonNull(achievementRepository);
        this.adaptivityConfiguration = Objects.requireNonNull(adaptivityConfiguration);
    }

    // Interface Implementation

    @Override
    public void onAchievementCompleted(AchievementEntity achievement, UserGoalProgressEntity goalProgressEntity) {
        AchievementCompletedEvent event = AchievementCompletedEvent.builder()
                .userId(goalProgressEntity.getUser().getId())
                .achievementId(achievement.getId())
                .courseId(achievement.getCourse().getId())
                .build();
        topicPublisher.notifyAchievementCompleted(event);
        appPublisher.publishEvent(new de.unistuttgart.iste.meitrex.gamification_service.events.internal.domain.AchievementCompletedEvent(this, achievement, goalProgressEntity.getUser()));
        addRewardToUser(goalProgressEntity.getUser());

        tryGenerateAdaptiveAchievementForUser(
                goalProgressEntity,
                achievement.getCourse(),
                achievement);
    }

    /**
     * Adds the achievementCompletionReward to the given user.
     * @param user userEntity that gets the achievementCompletedReward
     */
    private void addRewardToUser(UserEntity user) {
        user.getInventory().addPoints(achievementCompletionReward);
    }

    /**
     * Call this method with a user goal progress entity and the AchievementEntity of a completed achievement to
     * generate an adaptive achievement for the user in the course specified.
     */
    private void tryGenerateAdaptiveAchievementForUser(UserGoalProgressEntity goalProgress, CourseEntity course, AchievementEntity completedAchievement) {
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
    }

    /**
     * Helper method to check if a user has reached the maximum number of adaptive achievements.
     */
    private boolean hasUserMaxAdaptiveAchievements(UserEntity user) {
        long userAdaptiveAchievementCount = DefaultAchievementService.getAchievementsForUserEntity(user).stream()
                .filter(Achievement::getAdaptive)
                .count();
        return userAdaptiveAchievementCount >= adaptivityConfiguration.getMaxAdaptiveAchievementCount();
    }

}
