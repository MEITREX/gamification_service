package de.unistuttgart.iste.meitrex.gamification_service.achievements;

import de.unistuttgart.iste.meitrex.common.event.ForumActivityEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.*;

import java.util.ArrayList;
import java.util.List;

public class Achievements {

    public void generateAchievements(CourseEntity course) {
        List<AchievementEntity> achievements = new ArrayList<>();
        AchievementEntity quizMaster = generateQuizMaster(course);
        AchievementEntity forumAnswer = generateForumAnswerer(course);
        AchievementEntity loginAchievement = generateLoginAchievement(course);
        achievements.add(quizMaster);
        achievements.add(forumAnswer);
        achievements.add(loginAchievement);
        course.setAchievements(achievements);
    }

    public AchievementEntity generateQuizMaster(CourseEntity course) {
        AchievementEntity quizMaster = new AchievementEntity();
        quizMaster.setName("Quiz Master");
        CompletedQuizzesGoalEntity quizMasterGoal = new CompletedQuizzesGoalEntity();
        quizMasterGoal.setMinimumScore(1.0F);
        quizMasterGoal.setRequiredCount(1);
        quizMasterGoal.setAchievement(quizMaster);
        quizMaster.setGoal(quizMasterGoal);
        quizMaster.setCourse(course);
        return quizMaster;
    }

    public AchievementEntity generateForumAnswerer(CourseEntity course) {
        AchievementEntity forumAnswerer = new AchievementEntity();
        forumAnswerer.setName("Forum Answer");
        AnswerForumQuestionGoalEntity forumAnswerGoal = new AnswerForumQuestionGoalEntity();
        forumAnswerGoal.setRequiredCount(1);
        forumAnswerGoal.setAchievement(forumAnswerer);
        forumAnswerer.setGoal(forumAnswerGoal);
        forumAnswerer.setCourse(course);
        return forumAnswerer;
    }

    public AchievementEntity generateLoginAchievement(CourseEntity course) {
        AchievementEntity loginAchievement = new AchievementEntity();
        loginAchievement.setName("Login Achievement");
        LoginStreakGoalEntity loginStreakGoal = new LoginStreakGoalEntity();
        loginStreakGoal.setRequiredCount(2);
        loginStreakGoal.setAchievement(loginAchievement);
        loginAchievement.setGoal(loginStreakGoal);
        loginAchievement.setCourse(course);
        return loginAchievement;
    }
}
