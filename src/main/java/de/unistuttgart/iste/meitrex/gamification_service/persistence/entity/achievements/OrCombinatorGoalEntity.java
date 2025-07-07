package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;


@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Entity(name = "OrCombinatorGoal")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrCombinatorGoalEntity extends GoalEntity{

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    GoalEntity goal1;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    GoalEntity goal2;

    public String generateDescription() {
        return goal1.generateDescription().substring(0, goal1.generateDescription().length() - 1)+ " and " +
                goal2.generateDescription().substring(0, 1).toLowerCase() + goal2.generateDescription().substring(1);
    }

    public void updateProgress(UserGoalProgressEntity userGoalProgress) {
        if (userGoalProgress instanceof CombineUserGoalProgressEntity combineUserGoalProgress) {
            if (combineUserGoalProgress.getUserGoalProgressEntity1().isCompleted() || combineUserGoalProgress.getUserGoalProgressEntity2().isCompleted()){
                combineUserGoalProgress.setCompleted(true);
            }
        }
    }

    public void updateQuizProgress(CombineUserGoalProgressEntity userGoalProgress, Float progress, UUID contentId) {
        switch (goal1) {
            case AndCombinatorGoalEntity andCombinatorGoalEntity -> andCombinatorGoalEntity
                    .updateQuizProgress((CombineUserGoalProgressEntity) userGoalProgress.getUserGoalProgressEntity1(), progress, contentId);
            case OrCombinatorGoalEntity orCombinatorGoalEntity -> orCombinatorGoalEntity
                    .updateQuizProgress((CombineUserGoalProgressEntity) userGoalProgress.getUserGoalProgressEntity1(), progress, contentId);
            case CompletedQuizzesGoalEntity completedQuizzesGoalEntity -> completedQuizzesGoalEntity
                    .updateProgress((CountableUserGoalProgressEntity) userGoalProgress.getUserGoalProgressEntity1(), progress, contentId);
            default -> {}
        }

        switch (goal2) {
            case AndCombinatorGoalEntity andCombinatorGoalEntity -> andCombinatorGoalEntity
                    .updateQuizProgress((CombineUserGoalProgressEntity) userGoalProgress.getUserGoalProgressEntity2(), progress, contentId);
            case OrCombinatorGoalEntity orCombinatorGoalEntity -> orCombinatorGoalEntity
                    .updateQuizProgress((CombineUserGoalProgressEntity) userGoalProgress.getUserGoalProgressEntity2(), progress, contentId);
            case CompletedQuizzesGoalEntity completedQuizzesGoalEntity -> completedQuizzesGoalEntity
                    .updateProgress((CountableUserGoalProgressEntity) userGoalProgress.getUserGoalProgressEntity2(), progress, contentId);
            default -> {}
        }
        if (userGoalProgress.getUserGoalProgressEntity1().isCompleted() || userGoalProgress.getUserGoalProgressEntity2().isCompleted()){
            userGoalProgress.setCompleted(true);
        }
    }

    public void updateForumProgress(CombineUserGoalProgressEntity userGoalProgress){
        switch (goal1) {
            case AndCombinatorGoalEntity andCombinatorGoalEntity -> andCombinatorGoalEntity
                    .updateForumProgress((CombineUserGoalProgressEntity) userGoalProgress.getUserGoalProgressEntity1());
            case OrCombinatorGoalEntity orCombinatorGoalEntity -> orCombinatorGoalEntity
                    .updateForumProgress((CombineUserGoalProgressEntity) userGoalProgress.getUserGoalProgressEntity1());
            case AnswerForumQuestionGoalEntity answerForumQuestionGoal -> answerForumQuestionGoal.updateProgress(userGoalProgress.getUserGoalProgressEntity1());
            default -> {}
        }

        switch (goal2) {
            case AndCombinatorGoalEntity andCombinatorGoalEntity -> andCombinatorGoalEntity
                    .updateForumProgress((CombineUserGoalProgressEntity) userGoalProgress.getUserGoalProgressEntity2());
            case OrCombinatorGoalEntity orCombinatorGoalEntity -> orCombinatorGoalEntity
                    .updateForumProgress((CombineUserGoalProgressEntity) userGoalProgress.getUserGoalProgressEntity2());
            case AnswerForumQuestionGoalEntity answerForumQuestionGoal -> answerForumQuestionGoal.updateProgress(userGoalProgress.getUserGoalProgressEntity2());
            default -> {}
        }
        if (userGoalProgress.getUserGoalProgressEntity1().isCompleted() || userGoalProgress.getUserGoalProgressEntity2().isCompleted()){
            userGoalProgress.setCompleted(true);
        }
    }

    public void updateSpecificChapter(CombineUserGoalProgressEntity userGoalProgress, UUID chapterId){
        switch (goal1) {
            case AndCombinatorGoalEntity andCombinatorGoalEntity -> andCombinatorGoalEntity
                    .updateSpecificChapter((CombineUserGoalProgressEntity) userGoalProgress.getUserGoalProgressEntity1(), chapterId);
            case OrCombinatorGoalEntity orCombinatorGoalEntity -> orCombinatorGoalEntity
                    .updateSpecificChapter((CombineUserGoalProgressEntity) userGoalProgress.getUserGoalProgressEntity1(), chapterId);
            case CompleteSpecificChapterGoalEntity completeSpecificChapterGoalEntity -> completeSpecificChapterGoalEntity
                    .updateProgress(userGoalProgress.getUserGoalProgressEntity1(), chapterId);
            default -> {}
        }

        switch (goal2) {
            case AndCombinatorGoalEntity andCombinatorGoalEntity -> andCombinatorGoalEntity
                    .updateSpecificChapter((CombineUserGoalProgressEntity) userGoalProgress.getUserGoalProgressEntity2(), chapterId);
            case OrCombinatorGoalEntity orCombinatorGoalEntity -> orCombinatorGoalEntity
                    .updateSpecificChapter((CombineUserGoalProgressEntity) userGoalProgress.getUserGoalProgressEntity2(), chapterId);
            case CompleteSpecificChapterGoalEntity completeSpecificChapterGoalEntity -> completeSpecificChapterGoalEntity
                    .updateProgress(userGoalProgress.getUserGoalProgressEntity2(), chapterId);
            default -> {}
        }
        if (userGoalProgress.getUserGoalProgressEntity1().isCompleted() || userGoalProgress.getUserGoalProgressEntity2().isCompleted()){
            userGoalProgress.setCompleted(true);
        }
    }

    public void updateLoginStreak(CombineUserGoalProgressEntity userGoalProgress, OffsetDateTime loginTime){
        switch (goal1) {
            case AndCombinatorGoalEntity andCombinatorGoalEntity -> andCombinatorGoalEntity
                    .updateLoginStreak((CombineUserGoalProgressEntity) userGoalProgress.getUserGoalProgressEntity1(), loginTime);
            case OrCombinatorGoalEntity orCombinatorGoalEntity -> orCombinatorGoalEntity
                    .updateLoginStreak((CombineUserGoalProgressEntity) userGoalProgress.getUserGoalProgressEntity1(), loginTime);
            case LoginStreakGoalEntity loginStreakGoalEntity -> loginStreakGoalEntity
                    .updateProgress(userGoalProgress.getUserGoalProgressEntity1(), loginTime);
            default -> {}
        }

        switch (goal2) {
            case AndCombinatorGoalEntity andCombinatorGoalEntity -> andCombinatorGoalEntity
                    .updateLoginStreak((CombineUserGoalProgressEntity) userGoalProgress.getUserGoalProgressEntity2(), loginTime);
            case OrCombinatorGoalEntity orCombinatorGoalEntity -> orCombinatorGoalEntity
                    .updateLoginStreak((CombineUserGoalProgressEntity) userGoalProgress.getUserGoalProgressEntity2(), loginTime);
            case LoginStreakGoalEntity loginStreakGoalEntity -> loginStreakGoalEntity
                    .updateProgress(userGoalProgress.getUserGoalProgressEntity2(), loginTime);
            default -> {}
        }
        if (userGoalProgress.getUserGoalProgressEntity1().isCompleted() || userGoalProgress.getUserGoalProgressEntity2().isCompleted()){
            userGoalProgress.setCompleted(true);
        }
    }

    //@Override
    @Override
    public UserGoalProgressEntity generateUserGoalProgress(UserEntity user) {
        return new CombineUserGoalProgressEntity(user, this,
                goal1.generateUserGoalProgress(user),
                goal2.generateUserGoalProgress(user));
    }
}
