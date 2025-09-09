package de.unistuttgart.iste.meitrex.gamification_service.service.internal.quests.quest_generation;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestEntity;
import de.unistuttgart.iste.meitrex.gamification_service.quests.DailyQuestType;
import de.unistuttgart.iste.meitrex.gamification_service.recommendation.RecommendationType;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.recommendation.IRecommendationCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.quests.quest_generation.specialty_quest_goal_generation.ISpecialtyQuestGoalGenerator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.quests.quest_generation.specialty_quest_goal_generation.SpecialtyQuestGoalGeneratorFactory;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpecialtyDailyQuestGeneratorService implements IDailyQuestGenerator {

    private final IRecommendationCreator recommendationCreator;
    private final SpecialtyQuestGoalGeneratorFactory specialtyQuestGoalGeneratorFactory;

    @Override
    public Optional<QuestEntity> generateQuest(CourseEntity courseEntity,
                                               UserEntity userEntity,
                                               List<QuestEntity> otherQuests) {
        GamificationCategory recommendationCategory = recommendationCreator.makeRecommendation(
                userEntity.getId(), courseEntity.getId(), RecommendationType.DAILY_QUEST);

        log.info("Generating specialty daily quest for user {} in course {} with recommendation category {}",
                userEntity.getId(), courseEntity.getId(), recommendationCategory);

        ISpecialtyQuestGoalGenerator goalGenerator =
                specialtyQuestGoalGeneratorFactory.getGoalGenerator(recommendationCategory);

        Optional<GoalEntity> goal = goalGenerator.generateGoal(userEntity, courseEntity);

        if(goal.isEmpty())
            return Optional.empty();

        QuestEntity quest = new QuestEntity();
        quest.setName(goalGenerator.getQuestTitle());
        quest.setImageUrl("");
        quest.setCourse(courseEntity);
        quest.setGoal(goal.get());
        goal.get().setParentWithGoal(quest);

        return Optional.of(quest);
    }

    @Override
    public DailyQuestType generatesQuestType() {
        return DailyQuestType.SPECIALTY;
    }
}
