package de.unistuttgart.iste.meitrex.gamification_service.service.quests;

import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.meitrex.gamification_service.config.AdaptivityConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserCourseDataEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestSetEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ICourseRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserRepository;
import de.unistuttgart.iste.meitrex.gamification_service.quests.DailyQuestType;
import de.unistuttgart.iste.meitrex.gamification_service.service.quests.quest_generation.QuestGeneratorServiceFactory;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestService implements IQuestService{

    private final IUserRepository userRepository;

    private final ICourseRepository courseRepository;

    private final AdaptivityConfiguration adaptivityConfiguration;

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
            courseData.setDailyQuestSet(generateDailyQuestSet(courseId, user, courseData.getDailyQuestSet()));
            userRepository.save(user); // Save the updated user with the new quest set
        }

        return courseData.getDailyQuestSet();
    }

    private QuestSetEntity generateDailyQuestSet(@NotNull final UUID courseId,
                                                 @NotNull final UserEntity user,
                                                 @Nullable final QuestSetEntity previousQuestSet) {
        log.info("Generating new daily quest set for user {} in course {}", user, courseId);

        CourseEntity courseEntity = courseRepository.findByIdOrThrow(courseId);

        float rewardMultiplier = previousQuestSet == null
                ? 1
                : (previousQuestSet.getRewardMultiplier() + 0.5f);

        int rewardPoints = (int)(adaptivityConfiguration.getQuestBaseRewardPoints() * rewardMultiplier);

        // TODO: Assign reward points to quests

        List<DailyQuestType> questTypeCandidates = Arrays.asList(DailyQuestType.values());
        Collections.shuffle(questTypeCandidates);

        List<QuestEntity> quests = new ArrayList<>();
        while (quests.size() < DAILY_QUEST_COUNT) {
            if (questTypeCandidates.isEmpty())
                break;

            DailyQuestType questType = questTypeCandidates.removeLast();
            try {
                Optional<QuestEntity> generatedQuestEntity = questGeneratorServiceFactory.getQuestGenerator(questType)
                        .generateQuest(courseEntity, user, Collections.unmodifiableList(quests));

                generatedQuestEntity.ifPresent(quests::add);
            } catch (ContentServiceConnectionException e) {
                throw new RuntimeException(e);
            }
        }

        LocalDate now = LocalDate.now();
        return QuestSetEntity.builder()
                .name("Daily Quest Set for " + now)
                .forDay(now)
                .quests(quests)
                .rewardMultiplier(rewardMultiplier)
                .build();
    }
}
