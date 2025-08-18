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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IQuestGenerator {
    Optional<QuestEntity> generateQuest(final CourseEntity courseEntity,
                                        final UserEntity userEntity,
                                        final List<QuestEntity> otherQuests)
            throws ContentServiceConnectionException;
    DailyQuestType generatesQuestType(); // Returns the type of quest this generator supports

    static List<Content> getContentsOfCourseNotInOtherQuests(final ContentServiceClient contentService,
                                                             final CourseEntity courseEntity,
                                                             final UserEntity userEntity,
                                                             final List<QuestEntity> otherQuests)
            throws ContentServiceConnectionException {
        List<Content> courseContents = contentService.queryContentsOfCourse(userEntity.getId(), courseEntity.getId());

        // remove contents that are already part of other quests, we don't want multiple quests for the same content
        List<UUID> contentsOfOtherQuests = getContentsOfOtherQuests(otherQuests);
        courseContents.removeIf(c -> contentsOfOtherQuests.contains(c.getId()));
        return courseContents;
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
