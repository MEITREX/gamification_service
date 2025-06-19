package de.unistuttgart.iste.meitrex.gamification_service.achievements;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.*;
import de.unistuttgart.iste.meitrex.generated.dto.Content;

import java.util.ArrayList;
import java.util.List;

public class Achievements {

    public void generateAchievements(CourseEntity course) {
        List<AchievementEntity> achievements = new ArrayList<>();
        AchievementEntity quizMaster = generateQuizMaster(course);
        achievements.add(quizMaster);
        course.setAchievements(achievements);
    }

    public AchievementEntity generateQuizMaster(CourseEntity course) {
        AchievementEntity quizMaster = new AchievementEntity();
        quizMaster.setName("Chapter Starter");
        CompletedQuizzesGoalEntity quizMasterGoal = new CompletedQuizzesGoalEntity();
        quizMasterGoal.setMinimumScore(1.0F);
        quizMasterGoal.setRequiredCount(1);
        quizMaster.setGoal(quizMasterGoal);
        quizMaster.setCourse(course);
        return quizMaster;
    }
}
