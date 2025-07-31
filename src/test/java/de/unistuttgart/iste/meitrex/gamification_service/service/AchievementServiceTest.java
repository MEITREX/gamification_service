package de.unistuttgart.iste.meitrex.gamification_service.service;


import de.unistuttgart.iste.meitrex.gamification_service.achievements.Achievements;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.CountableGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.CountableUserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.UserRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.AchievementService;
import de.unistuttgart.iste.meitrex.generated.dto.Achievement;
import de.unistuttgart.iste.meitrex.generated.dto.Chapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class AchievementServiceTest {
    private final UserRepository userRepository  = mock(UserRepository.class);

    private final Achievements achievements = new Achievements();

    AchievementService achievementService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        achievementService = new AchievementService(userRepository);
    }

    @Test
    void testGetAchievementsForUserInCourse() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        Chapter chapter = new Chapter();
        chapter.setId(UUID.randomUUID());
        chapter.setTitle("Chapter Title");
        chapter.setDescription("Chapter Description");
        List<Chapter> chapters = new ArrayList<>(List.of(chapter));
        CourseEntity courseEntity = CourseEntity.builder()
                .id(courseId)
                .chapters(chapters)
                .build();
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        courseEntity.setAchievements(achievementEntities);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setCourseIds(new ArrayList<>(List.of(courseId)));
        List<UserGoalProgressEntity> userGoalProgress = achievementEntities.stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity)).toList();
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        List<Achievement> achievementResult = achievementService.getAchievementsForUserInCourse(userId, courseId);
        List<Achievement> achievementList = new ArrayList<>();
        mapUserGoalProgressToAchievements(userEntity.getUserGoalProgressEntities(), achievementList);
        assertThat(achievementResult, is(achievementList));
    }

    @Test
    void testGetAchievementsForUser() {
        UUID userId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        Chapter chapter = new Chapter();
        chapter.setId(UUID.randomUUID());
        chapter.setTitle("Chapter Title");
        chapter.setDescription("Chapter Description");
        List<Chapter> chapters = new ArrayList<>(List.of(chapter));
        CourseEntity courseEntity = CourseEntity.builder()
                .id(courseId)
                .chapters(chapters)
                .build();
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        courseEntity.setAchievements(achievementEntities);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setCourseIds(new ArrayList<>(List.of(courseId)));
        List<UserGoalProgressEntity> userGoalProgress = achievementEntities.stream().map(achievement ->
                achievement.getGoal().generateUserGoalProgress(userEntity)).toList();
        userEntity.setUserGoalProgressEntities(userGoalProgress);
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        List<Achievement> achievementResult = achievementService.getAchievementsForUser(userId);
        List<Achievement> achievementList = new ArrayList<>();
        mapUserGoalProgressToAchievements(userEntity.getUserGoalProgressEntities(), achievementList);
        assertThat(achievementResult, is(achievementList));
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
                    }
                }
                userAchievements.add(achievement);
            }
        });
    }
}
