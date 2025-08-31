package de.unistuttgart.iste.meitrex.gamification_service.service.quests.quest_generation;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestEntity;
import de.unistuttgart.iste.meitrex.gamification_service.quests.DailyQuestType;
import de.unistuttgart.iste.meitrex.gamification_service.recommendation.RecommendationType;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.recommendation.IRecommendationCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.recommendation.IRecommendationService;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SpecialtyDailyQuestGeneratorService implements IDailyQuestGenerator {

    private final IRecommendationCreator recommendationCreator;

    @Override
    public Optional<QuestEntity> generateQuest(CourseEntity courseEntity,
                                               UserEntity userEntity,
                                               List<QuestEntity> otherQuests) {
        GamificationCategory recommendationCategory = recommendationCreator.makeRecommendation(
                userEntity.getId(), courseEntity.getId(), RecommendationType.DAILY_QUEST);

        Optional<GoalEntity> goal = generateGoalForCategory(recommendationCategory);

        if(goal.isEmpty())
            return Optional.empty();

        QuestEntity quest = new QuestEntity();
        quest.setName(getQuestName(recommendationCategory));
        quest.setImageUrl("");
        quest.setCourse(courseEntity);
        quest.setGoal(goal.get());
        goal.get().setParentWithGoal(quest);

        return Optional.empty();
    }

    private String getQuestName(GamificationCategory category) {
        return switch (category) {
            case ASSISTANCE -> "Stop it! Get some help!";
            case IMMERSION -> "Immerse yourself!";
            case RISK_REWARD -> "Gotta hit gold!";
            case CUSTOMIZATION -> "Put on some fresh clothes";
            case PROGRESSION -> "Get stuff done";
            case ALTRUISM -> "Help out some fellow learners";
            case SOCIALIZATION -> "Socialization Quest";
            case INCENTIVE -> "Collect 'em all!";
        };
    }

    private Optional<GoalEntity> generateGoalForCategory(GamificationCategory category) {
        return switch (category) {
            case ALTRUISM -> generateAltruismGoal();
            case SOCIALIZATION -> generateSocializationGoal();
            case ASSISTANCE -> generateAssistanceGoal();
            case IMMERSION -> generateImmersionGoal();
            case RISK_REWARD -> generateRiskRewardGoal();
            case CUSTOMIZATION -> generateCustomizationGoal();
            case PROGRESSION -> generateProgressionGoal();
            case INCENTIVE -> generateIncentiveGoal();
        };
    }

    private Optional<GoalEntity> generateAltruismGoal() {
        AnswerForumQuestionGoalEntity goal = new AnswerForumQuestionGoalEntity();
        goal.setTrackingTimeToToday();
        goal.setRequiredCount(1);
        return Optional.of(goal);
    }

    private Optional<GoalEntity> generateSocializationGoal() {
        // TODO: Should check if user at top of leaderboard already before generating this
        MoveUpLeaderboardGoalEntity goal = new MoveUpLeaderboardGoalEntity();
        goal.setTrackingTimeToToday();
        return Optional.of(goal);
    }

    private Optional<GoalEntity> generateAssistanceGoal() {
        // TODO: Implement this
        return Optional.empty();
    }

    private Optional<GoalEntity> generateImmersionGoal() {
        // We have no fitting quest for immersion. This is ok. Intentionally returning empty.
        return Optional.empty();
    }

    private Optional<GoalEntity> generateRiskRewardGoal() {
        LotteryRunGoalEntity goal = new LotteryRunGoalEntity();
        goal.setTrackingTimeToToday();
        goal.setRequiredCount(1);
        return Optional.of(goal);
    }

    private Optional<GoalEntity> generateCustomizationGoal() {
        EquipItemGoalEntity goal = new EquipItemGoalEntity();
        goal.setTrackingTimeToToday();
        return Optional.of(goal);
    }

    private Optional<GoalEntity> generateProgressionGoal() {
        // TODO: Implement this
        return Optional.empty();
    }

    private Optional<GoalEntity> generateIncentiveGoal() {
        ReceiveItemsGoalEntity goal = new ReceiveItemsGoalEntity();
        goal.setTrackingTimeToToday();
        goal.setRequiredCount(1);
        return Optional.of(goal);
    }

    @Override
    public DailyQuestType generatesQuestType() {
        return DailyQuestType.SPECIALTY;
    }
}
