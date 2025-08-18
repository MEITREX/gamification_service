package de.unistuttgart.iste.meitrex.gamification_service.service.quests.quest_generation;

import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.CompleteSpecificContentGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestEntity;
import de.unistuttgart.iste.meitrex.gamification_service.quests.DailyQuestType;
import de.unistuttgart.iste.meitrex.generated.dto.Assessment;
import de.unistuttgart.iste.meitrex.generated.dto.Content;
import de.unistuttgart.iste.meitrex.generated.dto.ProgressLogItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ExerciseDailyQuestGeneratorService implements IQuestGenerator {
    private final ContentServiceClient contentService;

    private Map<AssessmentMapKey, Assessment> foundAssessments;

    public Optional<QuestEntity> generateQuest(final CourseEntity courseEntity,
                                               final UserEntity userEntity,
                                               final List<QuestEntity> otherQuests)
            throws ContentServiceConnectionException {
        List<Content> courseContents = IQuestGenerator.getContentsOfCourseNotInOtherQuests(
                contentService, courseEntity, userEntity, otherQuests);

        // assessments for which the suggested date has passed and which the user has already unlocked
        // i.e. these need to be completed
        Stream<Assessment> assessmentsToDo = courseContents.stream()
                .filter(c -> c instanceof Assessment)
                .map(c -> (Assessment) c)
                .filter(a -> a.getMetadata().getSuggestedDate().isBefore(OffsetDateTime.now()))
                .filter(Assessment::getIsAvailableToBeWorkedOn);

        foundAssessments = new HashMap<>();

        assessmentsToDo.forEach(this::foundAssessment);

        Optional<Assessment> assessmentForQuest = decideMostImportantAssessment();
        if (assessmentForQuest.isEmpty())
            return Optional.empty(); // No assessments found that need to be completed

        QuestEntity quest = new QuestEntity();
        quest.setName("Daily Exercise");
        quest.setImageUrl("");
        quest.setCourse(courseEntity);

        CompleteSpecificContentGoalEntity goal = new CompleteSpecificContentGoalEntity();
        goal.setTrackingTimeToToday();
        goal.setParentWithGoal(quest);
        goal.setContentId(assessmentForQuest.get().getId());
        goal.setContentName(assessmentForQuest.get().getMetadata().getName());

        quest.setGoal(goal);

        return Optional.of(quest);
    }

    @Override
    public DailyQuestType generatesQuestType() {
        return DailyQuestType.EXERCISE;
    }

    private Optional<Assessment> decideMostImportantAssessment() {
        List<AssessmentMapKey> importanceOrder = List.of(
            new AssessmentMapKey(AssessmentUserStatus.FAILED, true),
            new AssessmentMapKey(AssessmentUserStatus.NOT_WORKED_ON, true),
            new AssessmentMapKey(AssessmentUserStatus.FAILED, false),
            new AssessmentMapKey(AssessmentUserStatus.NOT_WORKED_ON, false),
            new AssessmentMapKey(AssessmentUserStatus.PASSED_PAST_LEARNING_INTERVAL, true),
            new AssessmentMapKey(AssessmentUserStatus.PASSED_PAST_LEARNING_INTERVAL, false)
        );

        for (AssessmentMapKey key : importanceOrder) {
            if (foundAssessments.containsKey(key)) {
                return Optional.of(foundAssessments.get(key));
            }
        }

        return Optional.empty();
    }

    private void foundAssessment(Assessment assessment) {
        boolean required = assessment.getRequired();
        AssessmentUserStatus status = getAssessmentUserStatus(assessment);
        OffsetDateTime suggestedDate = assessment.getMetadata().getSuggestedDate();

        AssessmentMapKey key = new AssessmentMapKey(status, required);

        if(!foundAssessments.containsKey(key)) {
            foundAssessments.put(key, assessment);
        } else {
            Assessment existingAssessment = foundAssessments.get(key);
            // If the suggested date of the new assessment is earlier, replace the existing one
            if (suggestedDate.isBefore(existingAssessment.getMetadata().getSuggestedDate())) {
                foundAssessments.put(key, assessment);
            }
        }
    }

    private AssessmentUserStatus getAssessmentUserStatus(Assessment assessment) {
        Optional<ProgressLogItem> latestLog = assessment.getUserProgressData().getLog().stream()
                .max(Comparator.comparingLong(li -> li.getTimestamp().toEpochSecond()));

        if(latestLog.isEmpty())
            return AssessmentUserStatus.NOT_WORKED_ON;

        if(!latestLog.get().getSuccess())
            return AssessmentUserStatus.FAILED;

        if(assessment.getUserProgressData().getNextLearnDate().isBefore(OffsetDateTime.now()))
            return AssessmentUserStatus.PASSED_PAST_LEARNING_INTERVAL;

        return AssessmentUserStatus.PASSED_PAST_LEARNING_INTERVAL; // Default case, should not happen
    }

    private record AssessmentMapKey(AssessmentUserStatus status, boolean required) {
    }

    private enum AssessmentUserStatus {
        FAILED,                         // user has worked on the assessment and failed
        NOT_WORKED_ON,                  // user has not worked on the assessment yet
        PASSED_PAST_LEARNING_INTERVAL   // user has passed the assessment before, but the learning interval has passed
    }
}