package de.unistuttgart.iste.meitrex.gamification_service.service.quests;

import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserCourseDataEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestSetEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.UserRepository;
import de.unistuttgart.iste.meitrex.gamification_service.quests.DailyQuestType;
import de.unistuttgart.iste.meitrex.gamification_service.service.quests.quest_generation.ExerciseDailyQuestGeneratorService;
import de.unistuttgart.iste.meitrex.gamification_service.service.quests.quest_generation.IQuestGenerator;
import de.unistuttgart.iste.meitrex.gamification_service.service.quests.quest_generation.QuestGeneratorServiceFactory;
import de.unistuttgart.iste.meitrex.generated.dto.Content;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestService {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    private final ContentServiceClient contentService;

    private final QuestGeneratorServiceFactory questGeneratorServiceFactory;

    private final int DAILY_QUEST_COUNT = 3; // Number of quests in a daily quest set

    public QuestSetEntity getDailyQuestSetForUser(final UUID courseId, final UUID userId) {
        log.info("Fetching daily quest set for user {} in course {}", userId, courseId);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        UserCourseDataEntity courseData = user.getCourseData(courseId)
                .orElseThrow(() -> new IllegalArgumentException("No course data found for user " + userId
                        + " in course " + courseId));

        // if user has no daily quest set, or if it outdated, generate a new one
        if (courseData.getDailyQuestSet() == null
                || courseData.getDailyQuestSet().getForDay().isBefore(LocalDate.now())) {
            courseData.setDailyQuestSet(generateDailyQuestSet(courseId, user));
            userRepository.save(user); // Save the updated user with the new quest set
        }

        return courseData.getDailyQuestSet();
    }

    private QuestSetEntity generateDailyQuestSet(final UUID courseId, final UserEntity user) {
        log.info("Generating new daily quest set for user {} in course {}", user, courseId);

        CourseEntity courseEntity = courseRepository.findByIdOrThrow(courseId);

        LocalDate now = LocalDate.now();

        List<DailyQuestType> questTypeCandidates = Arrays.asList(DailyQuestType.values());
        Collections.shuffle(questTypeCandidates);

        List<QuestEntity> quests = new ArrayList<>();

        while (quests.size() < DAILY_QUEST_COUNT) {
            if (questTypeCandidates.isEmpty())
                break;

            DailyQuestType questType = questTypeCandidates.removeLast();
            try {
                Optional<QuestEntity> generatedQuestEntity = questGeneratorServiceFactory.getQuestGenerator(questType)
                        .generateQuest(courseEntity, user);

                generatedQuestEntity.ifPresent(quests::add);
            } catch (ContentServiceConnectionException e) {
                throw new RuntimeException(e);
            }
        }

        return QuestSetEntity.builder()
                .name("Daily Quest Set for " + now)
                .forDay(now)
                .quests(quests)
                .build();
    }
}
