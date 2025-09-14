package de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.*;
import de.unistuttgart.iste.meitrex.generated.dto.Chapter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

@Component
class DefaultCourseAchievementMapper implements ICourseAchievementMapper {

    @FunctionalInterface
    interface IAchievementSource {
        Optional<AchievementEntity> create(CourseEntity course);
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
        this.achievementSourceList.add(this::generateFirstStartAchievement);
        this.achievementSourceList.add(this::generateGamblerAchievement);
        this.achievementSourceList.add(this::generateFastFashionAchievement);
        this.achievementSourceList.add(this::generateCuriousAchievement);
    }

    @Override
    public List<AchievementEntity> map(CourseEntity course) {
        Objects.requireNonNull(course);
        final Function<AchievementEntity, AchievementEntity> courseAchievementLinker = createLinker(course);
        return this.achievementSourceList
                .stream()
                .flatMap(source -> source.create(course).stream())
                .map(courseAchievementLinker)
                .toList();
    }

    // Sources

    public Optional<AchievementEntity> generateQuizMaster(CourseEntity course) {
        AchievementEntity quizMaster = create("Quiz Master", "");
        CompletedQuizzesGoalEntity quizMasterGoal = new CompletedQuizzesGoalEntity();
        quizMasterGoal.setMinimumScore(1.0F);
        quizMasterGoal.setRequiredCount(5);
        quizMaster.setGoal(quizMasterGoal);
        return Optional.of(quizMaster);
    }

    public Optional<AchievementEntity> generateForumAnswerer(CourseEntity course) {
        AchievementEntity forumAnswerer = create("Forum Answer", "");
        AnswerForumQuestionGoalEntity forumAnswerGoal = new AnswerForumQuestionGoalEntity();
        forumAnswerGoal.setRequiredCount(3);
        forumAnswerer.setGoal(forumAnswerGoal);
        return Optional.of(forumAnswerer);
    }

    public Optional<AchievementEntity> generateLoginAchievement(CourseEntity course) {
        AchievementEntity loginAchievement = create("Login Achievement", "");
        LoginStreakGoalEntity loginStreakGoal = new LoginStreakGoalEntity();
        loginStreakGoal.setRequiredCount(3);
        loginAchievement.setGoal(loginStreakGoal);
        return Optional.of(loginAchievement);
    }

    public Optional<AchievementEntity> generateQuizzerAchievement(CourseEntity course) {
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
        return Optional.of(quizzer);
    }

    public Optional<AchievementEntity> generateFirstStartAchievement(CourseEntity course) {
        AchievementEntity activeUser =  create("A First Start", "");
        LoginStreakGoalEntity loginStreakGoal = new LoginStreakGoalEntity();
        loginStreakGoal.setRequiredCount(1);
        CompleteSpecificChapterGoalEntity completeSpecificChapterGoalEntity = new CompleteSpecificChapterGoalEntity();
        Optional<Chapter> firstChapter = course.getChapters().stream()
                .min(Comparator.comparingInt(Chapter::getNumber));

        if (firstChapter.isEmpty())
            return Optional.empty();

        completeSpecificChapterGoalEntity.setChapterId(firstChapter.get().getId());
        completeSpecificChapterGoalEntity.setChapterName(firstChapter.get().getTitle());
        AndCombinatorGoalEntity andCombinatorGoalEntity = new AndCombinatorGoalEntity();
        andCombinatorGoalEntity.setGoal1(loginStreakGoal);
        andCombinatorGoalEntity.setGoal2(completeSpecificChapterGoalEntity);
        activeUser.setGoal(andCombinatorGoalEntity);
        return Optional.of(activeUser);
    }

    public Optional<AchievementEntity> generateGamblerAchievement(CourseEntity course) {
        AchievementEntity gamblerAchievement = create("Gambler", "");

        LotteryRunGoalEntity lotteryRunGoal = new LotteryRunGoalEntity();
        lotteryRunGoal.setRequiredCount(5);
        gamblerAchievement.setGoal(lotteryRunGoal);

        return Optional.of(gamblerAchievement);
    }

    public Optional<AchievementEntity> generateFastFashionAchievement(CourseEntity course) {
        AchievementEntity fastFashion = create("Fast Fashion", "");

        ReceiveItemsGoalEntity receiveItemsGoalEntity = new ReceiveItemsGoalEntity();
        receiveItemsGoalEntity.setRequiredCount(5);
        fastFashion.setGoal(receiveItemsGoalEntity);

        return Optional.of(fastFashion);
    }

    public Optional<AchievementEntity> generateCuriousAchievement(CourseEntity course) {
        AchievementEntity curiousAchievement = create("Curious", "");

        AskTutorCourseQuestionsGoalEntity askTutorCourseQuestionsGoalEntity = new AskTutorCourseQuestionsGoalEntity();
        askTutorCourseQuestionsGoalEntity.setRequiredCount(5);
        curiousAchievement.setGoal(askTutorCourseQuestionsGoalEntity);
        return Optional.of(curiousAchievement);
    }
}
