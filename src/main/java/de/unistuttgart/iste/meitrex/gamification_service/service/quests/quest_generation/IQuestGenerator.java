package de.unistuttgart.iste.meitrex.gamification_service.service.quests.quest_generation;

import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.HasGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.CompleteSpecificContentGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestEntity;
import de.unistuttgart.iste.meitrex.gamification_service.quests.DailyQuestType;
import de.unistuttgart.iste.meitrex.generated.dto.Content;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IQuestGenerator {
    Optional<QuestEntity> generateQuest(final CourseEntity courseEntity,
                                        final UserEntity userEntity,
                                        final List<QuestEntity> otherQuests)
            throws ContentServiceConnectionException;
    DailyQuestType generatesQuestType(); // Returns the type of quest this generator supports

    /**
     * This method retrieves all contents of a course that are not used in any of the other quests.
     */
    static List<Content> getContentsOfCourseNotInOtherQuests(final ContentServiceClient contentService,
                                                             final CourseEntity courseEntity,
                                                             final UserEntity userEntity,
                                                             final List<QuestEntity> otherQuests)
            throws ContentServiceConnectionException {
        List<Content> courseContents = contentService.queryContentsOfCourse(userEntity.getId(), courseEntity.getId());
        return filterContentsUsedInOtherQuests(courseContents, otherQuests);
    }

    /**
     * For a given list of contents and a list of other quests, this method returns a new list which contains all the
     * items from the original list that are not used in any of the other quests.
     */
    static List<Content> filterContentsUsedInOtherQuests(final List<Content> contents,
                                                         final List<QuestEntity> otherQuests) {
        List<UUID> contentsOfOtherQuests = getContentsOfOtherQuests(otherQuests);
        final List<Content> result = new ArrayList<>(contents);
        result.removeIf(c -> contentsOfOtherQuests.contains(c.getId()));
        return result;
    }

    private static List<UUID> getContentsOfOtherQuests(final List<QuestEntity> otherQuests) {
        return otherQuests.stream()
                .map(HasGoalEntity::getGoal)
                .filter(g -> g instanceof CompleteSpecificContentGoalEntity)
                .map(g -> ((CompleteSpecificContentGoalEntity)g).getContentId())
                .distinct()
                .toList();
    }
}
