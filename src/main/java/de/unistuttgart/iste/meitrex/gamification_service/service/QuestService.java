package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserCourseDataEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestSetEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.UserRepository;
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

        LocalDate now = LocalDate.now();

        List<DailyQuestType> questTypeCandidates = Arrays.asList(DailyQuestType.values());
        Collections.shuffle(questTypeCandidates);

        List<QuestEntity> quests = new ArrayList<>();

        while(quests.size() < DAILY_QUEST_COUNT) {
            if(questTypeCandidates.isEmpty())
                break;

            DailyQuestType questType = questTypeCandidates.removeLast();

            QuestEntity questEntity = switch (questType) {
                case EXERCISE -> generateExerciseDailyQuest(courseId, user);
                case SKILL_LEVEL -> generateSkillLevelDailyQuest(courseId, user);
                case LEARNING -> generateLearningDailyQuest(courseId, user);
                case SPECIALTY -> generateSpecialtyDailyQuest(courseId, user);
            };
            if (questEntity != null) {
                quests.add(questEntity);
            }
        }

        return QuestSetEntity.builder()
                .name("Daily Quest Set for " + now)
                .forDay(now)
                .quests(quests)
                .build();
    }

    private QuestEntity generateExerciseDailyQuest(final UUID courseId, final UserEntity user) {
        log.info("Generating exercise daily quest for user {} in course {}", user, courseId);
        return null;
    }

    private QuestEntity generateSkillLevelDailyQuest(final UUID courseId, final UserEntity user) {

    }

    private QuestEntity generateLearningDailyQuest(final UUID courseId, final UserEntity user) {

    }

    private QuestEntity generateSpecialtyDailyQuest(final UUID courseId, final UserEntity user) {

    }

    private enum DailyQuestType {
        EXERCISE,       // work on assessments not yet completed
        SKILL_LEVEL,    // work on assessments with low skill level
        LEARNING,       // learn new content (slides/videos)
        SPECIALTY       // unique quests depending on user's recommendation score ("player type")
    }
}
