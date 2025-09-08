package de.unistuttgart.iste.meitrex.gamification_service.service.internal.quests.quest_generation;

import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.gamification_service.config.AdaptivityConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.CompleteSpecificContentGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestEntity;
import de.unistuttgart.iste.meitrex.gamification_service.quests.DailyQuestType;
import de.unistuttgart.iste.meitrex.generated.dto.Assessment;
import de.unistuttgart.iste.meitrex.generated.dto.Content;
import de.unistuttgart.iste.meitrex.generated.dto.ProgressLogItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ExerciseDailyQuestGeneratorService implements IDailyQuestGenerator {
    private final ContentServiceClient contentService;
    private final AdaptivityConfiguration adaptivityConfiguration;

    /**
     * The order of importance for the assessments when generating the daily quest, from most important to least
     * important. Used to prioritize which assessments to suggest to the user.
     * This does not contain the "passed" assessments, as those are not relevant for the daily quest.
     */
    private final List<AssessmentMapKey> assessmentImportanceOrder = List.of(
            new AssessmentMapKey(AssessmentUserStatus.FAILED, true),
            new AssessmentMapKey(AssessmentUserStatus.NOT_WORKED_ON, true),
            new AssessmentMapKey(AssessmentUserStatus.FAILED, false),
            new AssessmentMapKey(AssessmentUserStatus.NOT_WORKED_ON, false),
            new AssessmentMapKey(AssessmentUserStatus.PASSED_PAST_LEARNING_INTERVAL, true),
            new AssessmentMapKey(AssessmentUserStatus.PASSED_PAST_LEARNING_INTERVAL, false)
    );

    public Optional<QuestEntity> generateQuest(final CourseEntity courseEntity,
                                               final UserEntity userEntity,
                                               final List<QuestEntity> otherQuests)
            throws ContentServiceConnectionException {
        final List<Content> courseContents = IDailyQuestGenerator.getContentsOfCourseNotInOtherQuests(
                contentService, courseEntity, userEntity, otherQuests);

        // assessments for which the suggested date has passed and which the user has already unlocked
        // i.e. these need to be completed
        final Stream<Assessment> assessmentsToDo = courseContents.stream()
                .filter(c -> c instanceof Assessment)
                .map(c -> (Assessment) c)
                .filter(a -> a.getMetadata().getSuggestedDate().isBefore(OffsetDateTime.now()))
                .filter(Assessment::getIsAvailableToBeWorkedOn)
                .sorted(Comparator.comparing(ass -> ass.getMetadata().getSuggestedDate()));

        // we put all the assessments we find into lists, according what importance they have
        // the order in the list is from the furthest back suggestedDate to most recent
        final Map<AssessmentMapKey, List<Assessment>> foundAssessments = new HashMap<>();
        for(final AssessmentMapKey key : assessmentImportanceOrder) {
            foundAssessments.put(key, new ArrayList<>());
        }
        assessmentsToDo.forEach(ass -> putFoundAssessment(foundAssessments, ass));

        // for assessments which the user passed but are past their learning interval, we want to sort them based on
        // how long the learning interval has been exceeded, instead of sorting by their suggestedDate like for the
        // other assessment categories.
        // This ensures that the assessment with the longest exceeded learning interval is first in the list and is
        // picked first by the assessment picking function
        foundAssessments.put(new AssessmentMapKey(AssessmentUserStatus.PASSED_PAST_LEARNING_INTERVAL, true),
                sortAssessmentsByExceededLearningIntervalDescending(foundAssessments.get(
                        new AssessmentMapKey(AssessmentUserStatus.PASSED_PAST_LEARNING_INTERVAL, true))));
        foundAssessments.put(new AssessmentMapKey(AssessmentUserStatus.PASSED_PAST_LEARNING_INTERVAL, false),
                sortAssessmentsByExceededLearningIntervalDescending(foundAssessments.get(
                        new AssessmentMapKey(AssessmentUserStatus.PASSED_PAST_LEARNING_INTERVAL, false))));

        final Optional<Assessment> assessmentForQuest = decideAssessment(foundAssessments);
        if (assessmentForQuest.isEmpty())
            return Optional.empty(); // No assessments found that need to be completed

        final QuestEntity quest = new QuestEntity();
        quest.setName("Daily Exercise");
        quest.setImageUrl("");
        quest.setCourse(courseEntity);

        CompleteSpecificContentGoalEntity goal = new CompleteSpecificContentGoalEntity();
        goal.setTrackingTimeToToday();
        goal.setParentWithGoal(quest);
        goal.setContentId(assessmentForQuest.get().getId());
        goal.setContentName(assessmentForQuest.get().getMetadata().getName());
        goal.setContentType(assessmentForQuest.get().getMetadata().getType());

        quest.setGoal(goal);

        return Optional.of(quest);
    }

    @Override
    public DailyQuestType generatesQuestType() {
        return DailyQuestType.EXERCISE;
    }

    /**
     * Helper method to decide which assessment from our found assessments should be picked.
     */
    private Optional<Assessment> decideAssessment(final Map<AssessmentMapKey, List<Assessment>> foundAssessments) {
        // we first try to pick an assessment with a "pickProbability" chance
        Optional<Assessment> pickedAssessment = pickAssessmentRandomly(
                foundAssessments, adaptivityConfiguration.getExerciseQuestRandomPickProbability());

        // if we get unlucky, try again with a pickProbability of 1.0f, to ensure we pick something (unless there are
        // no assessments at all)
        if(pickedAssessment.isEmpty())
            pickedAssessment = pickAssessmentRandomly(foundAssessments, 1.0f);

        return pickedAssessment;
    }

    /**
     * Helper method which picks an assessment randomly from the foundAssessments based on pickProbability.
     * There is a "pickProbability" chance the first bucket of foundAssessments is picked (according to the importance
     * order), otherwise the next bucket is picked with a "pickProbability" chance, and so on.
     * <br>
     * When a bucket has been decided, it will randomly pick one the first assessment in that bucket with a
     * "pickProbability" chance, otherwise the next one with a "pickProbability" chance, and so on.
     * If no assessment is picked, the last assessment in the bucket is returned.
     * If no bucket is picked, an empty Optional is returned.
     */
    private Optional<Assessment> pickAssessmentRandomly(final Map<AssessmentMapKey, List<Assessment>> foundAssessments,
                                                        final float pickProbability) {
        for (AssessmentMapKey key : assessmentImportanceOrder) {
            final List<Assessment> assessments = foundAssessments.get(key);
            if(Math.random() <= pickProbability && !assessments.isEmpty()) {
                for(Assessment assessment : assessments) {
                    if(Math.random() <= pickProbability) {
                        return Optional.of(assessment);
                    }
                }
                return Optional.of(assessments.getLast());
            }
        }

        return Optional.empty();
    }

    private void putFoundAssessment(final Map<AssessmentMapKey, List<Assessment>> foundAssessments,
                                    final Assessment assessment) {

        final boolean required = assessment.getRequired();
        final AssessmentUserStatus status = getAssessmentUserStatus(assessment);

        final AssessmentMapKey key = new AssessmentMapKey(status, required);

        if(!foundAssessments.containsKey(key)) {
            throw new IllegalStateException("Assessment key not found in the map: " + key + ". " +
                    "This indicates a bug in the quest generation logic.");
        }

        foundAssessments.get(key).add(assessment);
    }

    private AssessmentUserStatus getAssessmentUserStatus(final Assessment assessment) {
        final Optional<ProgressLogItem> latestLog = assessment.getUserProgressData().getLog().stream()
                .max(Comparator.comparingLong(li -> li.getTimestamp().toEpochSecond()));

        if(latestLog.isEmpty())
            return AssessmentUserStatus.NOT_WORKED_ON;

        if(!latestLog.get().getSuccess())
            return AssessmentUserStatus.FAILED;

        // if user has passed assessment, but no next learn interval is given, we will treat this assessment as
        // completely passed and will not suggest it
        if(assessment.getUserProgressData().getNextLearnDate() == null)
            return AssessmentUserStatus.PASSED;

        // otherwise, we check if the learning interval has passed and note this for this assessment. A passed learning
        // interval is a reason to suggest it
        if(assessment.getUserProgressData().getNextLearnDate().isBefore(OffsetDateTime.now()))
            return AssessmentUserStatus.PASSED_PAST_LEARNING_INTERVAL;

        // if learning interval has not passed yet, we treat it as passed and will not suggest it
        return AssessmentUserStatus.PASSED;
    }

    /**
     * Helper method which sorts the passed list of assessments by the exceeded learning interval in descending order,
     * i.e. the first element in the returned list is the assessment with the most exceeded learning interval.
     */
    private List<Assessment> sortAssessmentsByExceededLearningIntervalDescending(final List<Assessment> assessments) {
        return assessments.stream()
                .sorted(Comparator.comparing((Assessment ass) ->
                        Duration.between(ass.getUserProgressData().getNextLearnDate(), OffsetDateTime.now())).reversed())
                .toList();
    }

    private record AssessmentMapKey(AssessmentUserStatus status, boolean required) {
    }

    private enum AssessmentUserStatus {
        FAILED,                         // user has worked on the assessment and failed
        NOT_WORKED_ON,                  // user has not worked on the assessment yet
        PASSED_PAST_LEARNING_INTERVAL,  // user has passed the assessment before, but the learning interval has passed
        PASSED                          // use has passed assessment before and learning interval has not passed
    }
}