package de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.*;
import de.unistuttgart.iste.meitrex.generated.dto.Chapter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
class DefaultCourseAchievementMapper implements ICourseAchievementMapper {

    @FunctionalInterface
    interface IAchievementSource {
        AchievementEntity create(CourseEntity course);
    }

    // Functional constructors

    private static Function<AchievementEntity, AchievementEntity> createLinker(CourseEntity courseEntity) {
        return (achievementEntity -> {
            achievementEntity.setCourse(courseEntity);
            return achievementEntity;
        });
    }

    // Static helpers

    private static AchievementEntity create(String name, String imageUrl) {
        AchievementEntity quizMaster = new AchievementEntity();
        quizMaster.setName(name);
        quizMaster.setImageUrl(imageUrl);
        return quizMaster;
    }


    private final List<IAchievementSource> achievementSourceList = new ArrayList<>();

    public DefaultCourseAchievementMapper() {
        this.achievementSourceList.add(this::generateQuizMaster);
        this.achievementSourceList.add(this::generateForumAnswerer);
        this.achievementSourceList.add(this::generateLoginAchievement);
        this.achievementSourceList.add(this::generateQuizzerAchievement);
        this.achievementSourceList.add(this::generateActiveAchievement);

    }

    @Override
    public List<AchievementEntity> map(CourseEntity course) {
        Objects.requireNonNull(course);
        final Function<AchievementEntity, AchievementEntity> courseAchievementLinker = createLinker(course);
        return this.achievementSourceList
                .stream()
                .map(source -> source.create(course))
                .map(courseAchievementLinker)
                .toList();
    }

    // Sources

    public AchievementEntity generateQuizMaster(CourseEntity course) {
        AchievementEntity quizMaster = create("Quiz Master", "");
        CompletedQuizzesGoalEntity quizMasterGoal = new CompletedQuizzesGoalEntity();
        quizMasterGoal.setMinimumScore(1.0F);
        quizMasterGoal.setRequiredCount(1);
        quizMaster.setGoal(quizMasterGoal);
        return quizMaster;
    }

    public AchievementEntity generateForumAnswerer(CourseEntity course) {
        AchievementEntity forumAnswerer = create("Forum Answer", "");
        AnswerForumQuestionGoalEntity forumAnswerGoal = new AnswerForumQuestionGoalEntity();
        forumAnswerGoal.setRequiredCount(1);
        forumAnswerer.setGoal(forumAnswerGoal);
        return forumAnswerer;
    }

    public AchievementEntity generateLoginAchievement(CourseEntity course) {
        AchievementEntity loginAchievement = create("Login Achievement", "");
        LoginStreakGoalEntity loginStreakGoal = new LoginStreakGoalEntity();
        loginStreakGoal.setRequiredCount(2);
        loginAchievement.setGoal(loginStreakGoal);
        return loginAchievement;
    }

    public AchievementEntity generateQuizzerAchievement(CourseEntity course) {
        AchievementEntity quizzer = create("Quizzer", "");
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
        return quizzer;
    }

    public AchievementEntity generateActiveAchievement(CourseEntity course) {
        AchievementEntity activeUser =  create("Active User", "");
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
        return activeUser;
    }
}
