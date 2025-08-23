package de.unistuttgart.iste.meitrex.gamification_service.service.quests.quest_generation;

import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.gamification_service.config.AdaptivityConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.CompleteSpecificContentGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestEntity;
import de.unistuttgart.iste.meitrex.gamification_service.quests.DailyQuestType;
import de.unistuttgart.iste.meitrex.generated.dto.Content;
import de.unistuttgart.iste.meitrex.generated.dto.MediaContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class LearningDailyQuestGeneratorService implements IDailyQuestGenerator {
    private final ContentServiceClient contentService;
    private final AdaptivityConfiguration adaptivityConfiguration;

    @Override
    public Optional<QuestEntity> generateQuest(final CourseEntity courseEntity,
                                               final UserEntity userEntity,
                                               final List<QuestEntity> otherQuests)
            throws ContentServiceConnectionException {
        List<Content> courseContents = IDailyQuestGenerator.getContentsOfCourseNotInOtherQuests(
                contentService, courseEntity, userEntity, otherQuests);

        Optional<MediaContent> contentToGenerateQuestFor = getContentToGenerateQuestFor(courseContents);

        if (contentToGenerateQuestFor.isEmpty())
            return Optional.empty(); // No content found that needs to be learned

        QuestEntity quest = new QuestEntity();
        quest.setName("Daily Learning");
        quest.setImageUrl("");
        quest.setCourse(courseEntity);

        CompleteSpecificContentGoalEntity goal = new CompleteSpecificContentGoalEntity();
        goal.setTrackingTimeToToday();
        goal.setParentWithGoal(quest);
        goal.setContentId(contentToGenerateQuestFor.get().getId());
        goal.setContentName(contentToGenerateQuestFor.get().getMetadata().getName());
        goal.setContentType(contentToGenerateQuestFor.get().getMetadata().getType());

        quest.setGoal(goal);
        return Optional.of(quest);
    }

    @Override
    public DailyQuestType generatesQuestType() {
        return DailyQuestType.LEARNING;
    }

    /**
     * Helper method which, given the course contents, picks a content the user should work on, or an empty optional
     * if no suitable content was found.
     */
    private Optional<MediaContent> getContentToGenerateQuestFor(final List<Content> courseContents) {
        final List<MediaContent> requiredContents = new ArrayList<>();
        final List<MediaContent> optionalContents = new ArrayList<>();

        sortContentsIntoRequiredOptional(courseContents, requiredContents, optionalContents);

        Optional<MediaContent> pickedContent = pickRequiredOrOptionalContent(
                requiredContents, optionalContents, adaptivityConfiguration.getLearningQuestRandomPickProbability());

        if(pickedContent.isEmpty()) {
            pickedContent = pickRequiredOrOptionalContent(requiredContents, optionalContents, 1);
        }

        return pickedContent;
    }

    /**
     * Helper method to pick a content from the required and optional content lists with the given pickProbability.
     * The chance to pick a required content is pickProbability, the chance to pick an optional content is
     * (1 - pickProbability).
     */
    private Optional<MediaContent> pickRequiredOrOptionalContent(final List<MediaContent> requiredContents,
                                                                 final List<MediaContent> optionalContents,
                                                                 final float pickProbability) {
        Optional<MediaContent> picked = pickContentFromList(requiredContents, pickProbability);
        if(picked.isEmpty())
            picked = pickContentFromList(optionalContents, pickProbability);
        return picked;
    }

    /**
     * Helper method to pick a content from the given list with the given pickProbability, where the first content is
     * picked with pickProbability, the second content is picked with (1 - pickProbability) * pickProbability and so on.
     */
    private Optional<MediaContent> pickContentFromList(final List<MediaContent> contents,
                                                       final float pickProbability) {
        for (final MediaContent content : contents) {
            if(Math.random() <= pickProbability) {
                return Optional.of(content);
            }
        }
        if(!contents.isEmpty())
            return Optional.of(contents.getLast());

        return Optional.empty();
    }

    /**
     * Helper method which filters the media contents of the course and sorts them into required and optional ones
     */
    private static void sortContentsIntoRequiredOptional(final List<Content> courseContents,
                                                         final List<MediaContent> requiredContents,
                                                         final List<MediaContent> optionalContents) {
        // loop over the media contents that the user could potentially learn, in the order of newest to oldest
        // this ensures in the end we find the oldest one the user should learn
        courseContents.stream()
                .filter(c -> c instanceof MediaContent)
                .map(MediaContent.class::cast)
                .filter(c -> c.getMetadata().getSuggestedDate().isBefore(OffsetDateTime.now()))
                .filter(Content::getIsAvailableToBeWorkedOn)
                .filter(c -> !c.getUserProgressData().getIsLearned())
                .sorted(Comparator.comparing((Content c) -> c.getMetadata().getSuggestedDate()))
                .forEachOrdered(content -> {
                    if(content.getRequired())
                        requiredContents.add(content);
                    else
                        optionalContents.add(content);
                });
    }
}
