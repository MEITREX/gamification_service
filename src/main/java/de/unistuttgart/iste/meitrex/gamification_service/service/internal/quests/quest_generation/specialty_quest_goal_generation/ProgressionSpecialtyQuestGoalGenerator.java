package de.unistuttgart.iste.meitrex.gamification_service.service.internal.quests.quest_generation.specialty_quest_goal_generation;

import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.CompleteSpecificStageGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import de.unistuttgart.iste.meitrex.generated.dto.Section;
import de.unistuttgart.iste.meitrex.generated.dto.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProgressionSpecialtyQuestGoalGenerator implements ISpecialtyQuestGoalGenerator {

    private final ContentServiceClient contentService;

    @Override
    public GamificationCategory getCategory() {
        return GamificationCategory.PROGRESSION;
    }

    @Override
    public Optional<GoalEntity> generateGoal(UserEntity user, CourseEntity course) {
        // find an uncompleted stage in the course
        try {
            List<Section> potentialSections = contentService.querySectionsOfCourse(course.getId(), user.getId());
            List<Stage> potentialStages = potentialSections.stream()
                    .flatMap(sec -> sec.getStages().stream())
                    .filter(Stage::getIsAvailableToBeWorkedOn)
                    .filter(st -> st.getRequiredContentsProgress() < 100)
                    .toList();

            if(potentialStages.isEmpty())
                return Optional.empty();

            Stage selectedStage = potentialStages.get((int) (Math.random() * potentialStages.size()));
            Section sectionOfSelectedStage = potentialSections.stream()
                    .filter(sec -> sec.getStages().stream()
                            .anyMatch(st -> st.getId().equals(selectedStage.getId())))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Selected stage not found in any section. This should never happen."));

            CompleteSpecificStageGoalEntity goal = new CompleteSpecificStageGoalEntity();
            goal.setStageId(selectedStage.getId());
            goal.setStagePosition(selectedStage.getPosition());
            goal.setSectionName(sectionOfSelectedStage.getName());
            goal.setTrackingTimeToToday();
            return Optional.of(goal);
        } catch (ContentServiceConnectionException e) {
            log.error(
                    "Error while trying to generate progression specialty quest. Aborting and returning without quest.",
                    e);
            return Optional.empty();
        }
    }

    @Override
    public String getQuestTitle() {
        return "Get stuff done";
    }
}
