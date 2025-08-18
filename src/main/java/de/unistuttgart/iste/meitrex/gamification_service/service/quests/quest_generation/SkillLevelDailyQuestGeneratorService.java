package de.unistuttgart.iste.meitrex.gamification_service.service.quests.quest_generation;

import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.skilllevels.SkillLevelsEntity;
import de.unistuttgart.iste.meitrex.gamification_service.quests.DailyQuestType;
import de.unistuttgart.iste.meitrex.gamification_service.service.SkillService;
import de.unistuttgart.iste.meitrex.generated.dto.Assessment;
import de.unistuttgart.iste.meitrex.generated.dto.Content;
import de.unistuttgart.iste.meitrex.generated.dto.Skill;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SkillLevelDailyQuestGeneratorService implements IQuestGenerator {
    private final ContentServiceClient contentService;
    private final SkillService skillService;

    @Override
    public Optional<QuestEntity> generateQuest(final CourseEntity courseEntity,
                                               final UserEntity userEntity,
                                               final List<QuestEntity> otherQuests)
            throws ContentServiceConnectionException {
        List<Content> courseContents = IQuestGenerator.getContentsOfCourseNotInOtherQuests(
                contentService, courseEntity, userEntity, otherQuests);

        List<Assessment> assessments = courseContents.stream()
                .filter(c -> c instanceof Assessment)
                .map(Assessment.class::cast)
                .toList();

        List<Skill> skills = assessments.stream()
                .flatMap(a -> a.getItems().stream())
                .flatMap(i -> i.getAssociatedSkills().stream())
                .toList();

        List<SkillLevelsEntity> skillLevels = skills.stream()
                .flatMap(sk -> userEntity.getSkillLevelsForSkill(sk.getId()).stream())
                .toList();



        return Optional.empty();
    }

    @Override
    public DailyQuestType generatesQuestType() {
        return DailyQuestType.SKILL_LEVEL;
    }
}
