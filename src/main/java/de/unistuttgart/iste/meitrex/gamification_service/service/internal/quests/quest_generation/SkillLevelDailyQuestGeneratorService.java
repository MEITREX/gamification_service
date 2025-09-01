package de.unistuttgart.iste.meitrex.gamification_service.service.internal.quests.quest_generation;

import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.gamification_service.config.AdaptivityConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.CompleteSpecificContentGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.skilllevels.SkillEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.skilllevels.SkillLevelsEntity;
import de.unistuttgart.iste.meitrex.gamification_service.quests.DailyQuestType;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ISkillCreator;
import de.unistuttgart.iste.meitrex.generated.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillLevelDailyQuestGeneratorService implements IDailyQuestGenerator {

    private final ContentServiceClient contentService;
    private final AdaptivityConfiguration adaptivityConfiguration;
    private final ISkillCreator skillCreator;

    @Override
    public Optional<QuestEntity> generateQuest(final CourseEntity courseEntity,
                                               final UserEntity userEntity,
                                               final List<QuestEntity> otherQuests)
            throws ContentServiceConnectionException {
        Optional<Assessment> assessmentToUse = determineAssessmentToUse(courseEntity, userEntity, otherQuests);

        // if we could not find an assessment to use, give up generating a quest
        if (assessmentToUse.isEmpty())
            return Optional.empty();

        QuestEntity questEntity = new QuestEntity();
        questEntity.setName("Honing your skills in " + assessmentToUse.get().getMetadata().getName());
        questEntity.setImageUrl("");
        questEntity.setCourse(courseEntity);

        CompleteSpecificContentGoalEntity goalEntity = new CompleteSpecificContentGoalEntity();
        goalEntity.setTrackingTimeToToday();
        goalEntity.setParentWithGoal(questEntity);
        goalEntity.setContentId(assessmentToUse.get().getId());
        goalEntity.setContentName(assessmentToUse.get().getMetadata().getName());
        goalEntity.setContentType(assessmentToUse.get().getMetadata().getType());

        questEntity.setGoal(goalEntity);

        return Optional.of(questEntity);
    }

    @Override
    public DailyQuestType generatesQuestType() {
        return DailyQuestType.SKILL_LEVEL;
    }

    private Optional<Assessment> determineAssessmentToUse(final CourseEntity courseEntity,
                                                          final UserEntity userEntity,
                                                          final List<QuestEntity> otherQuests)
            throws ContentServiceConnectionException {
        List<Content> courseContents = contentService.queryContentsOfCourse(userEntity.getId(), courseEntity.getId());
        List<Assessment> courseAssessments = courseContents.stream()
                .filter(c -> c instanceof Assessment)
                .map(Assessment.class::cast)
                .toList();
        // get which assessments we can actually use for the quest, i.e. those which are not used in any of the
        // other quests, as it would make no sense to create 2 quests to complete the same assessment, and only
        // those assessments which are available to be worked on
        List<Assessment> usableCourseAssessments = IDailyQuestGenerator
                .filterContentsUsedInOtherQuests(courseContents, otherQuests).stream()
                .filter(Content::getIsAvailableToBeWorkedOn)
                .filter(c -> c instanceof Assessment)
                .map(Assessment.class::cast)
                .toList();

        List<Skill> skills = courseAssessments.stream()
                .flatMap(a -> a.getItems().stream())
                .flatMap(i -> i.getAssociatedSkills().stream())
                .toList();

        List<SkillLevelsEntity> skillLevels = skills.stream()
                .map(sk -> userEntity.getSkillLevelsForSkill(sk.getId())
                        .orElseGet(() -> {
                            SkillEntity skillEntity = skillCreator.fetchOrCreate(sk.getId());
                            return new SkillLevelsEntity(skillEntity, userEntity);
                        }))
                .toList();

        // calculate a "total" value for a skill level by averaging the bloom levels, but ignore bloom levels which
        // the user cannot achieve because there are no assessments associated with them
        List<Float> skillLevelsTotals = skillLevels.stream()
                .map(sl -> {
                    float sum = 0;
                    int counter = 0;
                    for(BloomLevel bloomLevel : BloomLevel.values()) {
                        boolean hasContent = bloomLevelHasAssociatedAssessmentsForSkill(
                                sl.getId(), bloomLevel, courseAssessments);
                        if(hasContent) {
                            sum += sl.getBloomLevelValue(bloomLevel);
                            counter++;
                        }
                    }
                    return sum / counter;
                })
                .toList();

        // when we decide on a skill to generate a quest for, it might happen that a quest cannot be generated for that
        // skill, e.g. because there are no assessments associated with the skill and the bloom level we want to use.
        // In that case, we retry up to 3 times to find a skill and bloom level combination that works.
        // If we still cannot find a suitable skill and bloom level, we give up
        for(int retryCount = 0; retryCount < 3; retryCount++) {
            // decide which skill to use for the quest by randomly selecting one of the skills, weighing the selection
            // using (1 - skillLevelTotal) as the weight, so that skills with a lower total are more likely to be selected
            SkillLevelsEntity skillLevelsToUse = skillLevels.get(chooseRandomIndexWeighted(skillLevelsTotals.stream()
                    .map(w -> 1 - w).toList()));

            // find the first bloom level which has a skill level below the threshold (that also has associated assessments)
            BloomLevel bloomLevelToUse = null;
            for(BloomLevel bloomLevel : BloomLevel.values()) {
                if(!bloomLevelHasAssociatedAssessmentsForSkill(
                        skillLevelsToUse.getSkill().getId(), bloomLevel, courseAssessments))
                    continue;

                if(skillLevelsToUse.getBloomLevelValue(bloomLevel) >=
                        adaptivityConfiguration.getSkillLevelQuestMinBloomScore())
                    continue;

                bloomLevelToUse = bloomLevel;
                break;
            }

            if(bloomLevelToUse == null)
                continue;

            // get candidates for assessments we could use for the quest
            List<Assessment> assessmentsWithSkillAndBloomLevel = filterAssessmentsWithAssociatedSkillAndBloomLevel(
                    skillLevelsToUse.getSkill().getId(),
                    bloomLevelToUse,
                    usableCourseAssessments
            );

            if(assessmentsWithSkillAndBloomLevel.isEmpty())
                continue;

            // pick an assessment randomly from the candidates, based on (1 - correctness score of the assessment) as weight
            List<Float> assessmentWeights = new ArrayList<>();
            for(Assessment assessment : assessmentsWithSkillAndBloomLevel) {
                Optional<ProgressLogItem> latestLog =
                        assessment.getUserProgressData().getLog().stream()
                                .max(Comparator.comparing(ProgressLogItem::getTimestamp));

                latestLog.ifPresentOrElse(
                        li -> assessmentWeights.add(1f - (float)li.getCorrectness()),
                        () -> assessmentWeights.add(1.0f) // no log item exists, assume the user has not attempted it yet
                );
            }

            Assessment assessmentToUse = assessmentsWithSkillAndBloomLevel.get(
                    chooseRandomIndexWeighted(assessmentWeights));

            return Optional.of(assessmentToUse);
        }

        return Optional.empty();
    }

    /**
     * @return true if there exists an assessment in the given list which has the given skill and bloom level
     * associated with it.
     */
    private boolean bloomLevelHasAssociatedAssessmentsForSkill(final UUID skillId,
                                                           final BloomLevel bloomLevel,
                                                           final List<Assessment> assessments) {
        return !filterAssessmentsWithAssociatedSkillAndBloomLevel(skillId, bloomLevel, assessments).isEmpty();
    }

    private List<Assessment> filterAssessmentsWithAssociatedSkillAndBloomLevel(final UUID skillId,
                                                                               final BloomLevel bloomLevel,
                                                                               final List<Assessment> assessments) {
        return assessments.stream()
                .filter(ass -> ass.getItems().stream()
                        .anyMatch(it -> it.getAssociatedBloomLevels().contains(bloomLevel)
                        && it.getAssociatedSkills().stream().anyMatch(sk -> sk.getId().equals(skillId))))
                .toList();
    }

    /**
     * Helper method which returns a randomly selected index in [0, weights.size()], chosen using the floats in the
     * "weights" list as weights.
     */
    private int chooseRandomIndexWeighted(List<Float> weights) {
        float totalWeight = 0;
        for(float weight : weights) {
            totalWeight += weight;
        }

        int index = 0;
        for (double r = Math.random() * totalWeight; index < weights.size() - 1; ++index) {
            r -= weights.get(index);
            if (r <= 0.0) break;
        }

        return index;
    }
}
