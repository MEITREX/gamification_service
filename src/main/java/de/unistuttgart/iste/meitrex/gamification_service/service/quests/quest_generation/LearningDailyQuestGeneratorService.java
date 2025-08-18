package de.unistuttgart.iste.meitrex.gamification_service.service.quests.quest_generation;

import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.CompleteSpecificContentGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestEntity;
import de.unistuttgart.iste.meitrex.gamification_service.quests.DailyQuestType;
import de.unistuttgart.iste.meitrex.generated.dto.Content;
import de.unistuttgart.iste.meitrex.generated.dto.MediaContent;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
public class LearningDailyQuestGeneratorService implements IQuestGenerator {
    private final ContentServiceClient contentService;

    @Override
    public Optional<QuestEntity> generateQuest(final CourseEntity courseEntity,
                                               final UserEntity userEntity,
                                               final List<QuestEntity> otherQuests)
            throws ContentServiceConnectionException {
        List<Content> courseContents = IQuestGenerator.getContentsOfCourseNotInOtherQuests(
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

    private Optional<MediaContent> getContentToGenerateQuestFor(final List<Content> courseContents) {
        final AtomicReference<MediaContent> oldestRequiredContent = new AtomicReference<>();
        final AtomicReference<MediaContent> oldestOptionalContent = new AtomicReference<>();

        // loop over the media contents that the user could potentially learn, in the order of newest to oldest
        // this ensures in the end we find the oldest one the user should learn
        courseContents.stream()
                .filter(c -> c instanceof MediaContent)
                .map(MediaContent.class::cast)
                .filter(c -> c.getMetadata().getSuggestedDate().isBefore(OffsetDateTime.now()))
                .filter(Content::getIsAvailableToBeWorkedOn)
                .filter(c -> !c.getUserProgressData().getIsLearned())
                .sorted(Comparator.comparing((Content c) -> c.getMetadata().getSuggestedDate()).reversed())
                .forEachOrdered(content -> {
                    if(content.getRequired())
                        oldestRequiredContent.set(content);
                    else
                        oldestOptionalContent.set(content);
                });

        if(oldestRequiredContent.get() != null)
            return Optional.of(oldestRequiredContent.get());
        else if (oldestOptionalContent.get() != null)
            return Optional.of(oldestOptionalContent.get());
        else
            return Optional.empty();
    }
}
