package de.unistuttgart.iste.meitrex.gamification_service.quests;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.CompleteSpecificAssessmentGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestEntity;
import de.unistuttgart.iste.meitrex.generated.dto.Assessment;
import de.unistuttgart.iste.meitrex.generated.dto.Content;
import de.unistuttgart.iste.meitrex.generated.dto.ProgressLogItem;
import lombok.RequiredArgsConstructor;

import java.time.*;
import java.util.*;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class ExerciseDailyQuestGenerator {
    private final CourseEntity courseEntity;
    private Map<AssessmentMapKey, Assessment> foundAssessments;

    public Optional<QuestEntity> generateExerciseDailyQuest(final List<Content> courseContents) {
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

        ZoneId zoneId = ZoneId.systemDefault();
        ZoneOffset offset = zoneId.getRules().getOffset(Instant.now());

        CompleteSpecificAssessmentGoalEntity goal = new CompleteSpecificAssessmentGoalEntity();
        goal.setTrackingStartTime(LocalDate.now().atStartOfDay().atOffset(offset));
        goal.setTrackingEndTime(LocalDate.now().atTime(LocalTime.MAX).atOffset(offset));
        goal.setParentWithGoal(quest);
        goal.setAssessmentId(assessmentForQuest.get().getId());
        goal.setAssessmentName(assessmentForQuest.get().getMetadata().getName());

        quest.setGoal(goal);

        return Optional.of(quest);
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