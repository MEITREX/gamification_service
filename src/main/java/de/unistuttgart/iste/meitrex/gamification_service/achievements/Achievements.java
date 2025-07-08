package de.unistuttgart.iste.meitrex.gamification_service.achievements;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.*;
import de.unistuttgart.iste.meitrex.generated.dto.Chapter;
import jakarta.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Achievements {

    public List<AchievementEntity> generateAchievements(CourseEntity course) {
        List<AchievementEntity> achievements = new ArrayList<>();
        AchievementEntity quizMaster = generateQuizMaster(course);
        AchievementEntity forumAnswer = generateForumAnswerer(course);
        AchievementEntity loginAchievement = generateLoginAchievement(course);
        AchievementEntity quizzer = generateQuizzerAchievement(course);
        AchievementEntity activeUser = generateActiveAchievement(course);
        achievements.add(quizMaster);
        achievements.add(forumAnswer);
        achievements.add(loginAchievement);
        achievements.add(quizzer);
        achievements.add(activeUser);
        return achievements;
    }

    public AchievementEntity generateQuizMaster(CourseEntity course) {
        AchievementEntity quizMaster = new AchievementEntity();
        quizMaster.setName("Quiz Master");
        CompletedQuizzesGoalEntity quizMasterGoal = new CompletedQuizzesGoalEntity();
        quizMasterGoal.setMinimumScore(1.0F);
        quizMasterGoal.setRequiredCount(1);
        quizMaster.setGoal(quizMasterGoal);
        quizMaster.setCourse(course);
        return quizMaster;
    }

    public AchievementEntity generateForumAnswerer(CourseEntity course) {
        AchievementEntity forumAnswerer = new AchievementEntity();
        forumAnswerer.setName("Forum Answer");
        AnswerForumQuestionGoalEntity forumAnswerGoal = new AnswerForumQuestionGoalEntity();
        forumAnswerGoal.setRequiredCount(1);
        forumAnswerer.setGoal(forumAnswerGoal);
        forumAnswerer.setCourse(course);
        return forumAnswerer;
    }

    public AchievementEntity generateLoginAchievement(CourseEntity course) {
        AchievementEntity loginAchievement = new AchievementEntity();
        loginAchievement.setName("Login Achievement");
        LoginStreakGoalEntity loginStreakGoal = new LoginStreakGoalEntity();
        loginStreakGoal.setRequiredCount(2);
        loginAchievement.setGoal(loginStreakGoal);
        loginAchievement.setCourse(course);
        return loginAchievement;
    }

    public AchievementEntity generateQuizzerAchievement(CourseEntity course) {
        AchievementEntity quizzer = new AchievementEntity();
        quizzer.setName("Quizzer");
        CompletedQuizzesGoalEntity quizzerGoal1 = new CompletedQuizzesGoalEntity();
        quizzerGoal1.setMinimumScore(1.0F);
        quizzerGoal1.setRequiredCount(2);
        CompletedQuizzesGoalEntity quizzerGoal2 = new CompletedQuizzesGoalEntity();
        quizzerGoal2.setMinimumScore(0.5F);
        quizzerGoal2.setRequiredCount(4);
        OrCombinatorGoalEntity orCombinatorGoalEntity = new OrCombinatorGoalEntity();
        orCombinatorGoalEntity.setGoal1(quizzerGoal1);
        orCombinatorGoalEntity.setGoal2(quizzerGoal2);
        quizzer.setGoal(orCombinatorGoalEntity);
        quizzer.setCourse(course);
        return quizzer;
    }

    public AchievementEntity generateActiveAchievement(CourseEntity course) {
        AchievementEntity activeUser = new AchievementEntity();
        activeUser.setName("Active User");
        LoginStreakGoalEntity loginStreakGoal = new LoginStreakGoalEntity();
        loginStreakGoal.setRequiredCount(7);
        CompleteSpecificChapterGoalEntity completeSpecificChapterGoalEntity = new CompleteSpecificChapterGoalEntity();
        Chapter firstChapter = course.getChapters().stream()
                .min(Comparator.comparingInt(Chapter::getNumber))
                .orElseThrow(() -> new EntityNotFoundException("Chapter List empty"));
        completeSpecificChapterGoalEntity.setChapterId(firstChapter.getId());
        completeSpecificChapterGoalEntity.setChapterName(firstChapter.getTitle());
        AndCombinatorGoalEntity andCombinatorGoalEntity = new AndCombinatorGoalEntity();
        andCombinatorGoalEntity.setGoal1(loginStreakGoal);
        andCombinatorGoalEntity.setGoal2(completeSpecificChapterGoalEntity);
        activeUser.setGoal(andCombinatorGoalEntity);
        activeUser.setCourse(course);
        return activeUser;
    }
}
