package de.unistuttgart.iste.meitrex.gamification_service.achievements;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.AchievementRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.GoalRepository;
import de.unistuttgart.iste.meitrex.generated.dto.Content;

import java.util.ArrayList;
import java.util.List;

public class Achievements {

    public void generateAchievements(CourseEntity course, AchievementRepository achievementRepository, GoalRepository goalRepository) {
        List<AchievementEntity> achievements = new ArrayList<>();
        AchievementEntity quizMaster = generateQuizMaster(course, achievementRepository, goalRepository);
        achievements.add(quizMaster);
        course.setAchievements(achievements);
    }

    public AchievementEntity generateQuizMaster(CourseEntity course, AchievementRepository achievementRepository, GoalRepository goalRepository) {
        AchievementEntity quizMaster = new AchievementEntity();
        quizMaster.setName("Quiz Master");
        achievementRepository.save(quizMaster);
        CompletedQuizzesGoalEntity quizMasterGoal = new CompletedQuizzesGoalEntity();
        quizMasterGoal.setMinimumScore(1.0F);
        quizMasterGoal.setRequiredCount(1);
        quizMasterGoal.setAchievement(quizMaster);
        goalRepository.save(quizMasterGoal);
        quizMaster.setGoal(quizMasterGoal);
        quizMaster.setCourse(course);
        achievementRepository.save(quizMaster);
        return quizMaster;
    }
}
