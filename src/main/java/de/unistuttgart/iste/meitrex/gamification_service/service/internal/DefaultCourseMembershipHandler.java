package de.unistuttgart.iste.meitrex.gamification_service.service.internal;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserCourseDataEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
class DefaultCourseMembershipHandler implements ICourseMembershipHandler {

    @Override
    public UserCourseDataEntity addUserToCourseIfNotAlready(CourseEntity course, UserEntity user) {
        Optional<UserCourseDataEntity> userCourseData = user.getCourseData(course.getId());
        if(userCourseData.isEmpty()) {
            List<UserGoalProgressEntity> userGoalProgressEntities = new ArrayList<>();
            for (AchievementEntity achievement : course.getAchievements()) {
                if (achievement.isAdaptive())
                    continue;
                UserGoalProgressEntity userGoalProgressEntity = achievement.getGoal().generateUserGoalProgress(user);
                userGoalProgressEntities.add(userGoalProgressEntity);
            }
            userCourseData = Optional.of(UserCourseDataEntity.builder()
                    .courseId(course.getId())
                    .goalProgressEntities(userGoalProgressEntities)
                    .dailyQuestSet(null)
                    .build());
            user.getCourseData().add(userCourseData.get());
        }
        return userCourseData.get();
    }

}
