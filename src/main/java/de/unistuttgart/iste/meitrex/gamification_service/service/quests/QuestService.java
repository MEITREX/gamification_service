package de.unistuttgart.iste.meitrex.gamification_service.service.quests;

import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.meitrex.gamification_service.config.AdaptivityConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserCourseDataEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.CountableGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.CountableUserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestSetEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ICourseRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserRepository;
import de.unistuttgart.iste.meitrex.gamification_service.quests.DailyQuestType;
import de.unistuttgart.iste.meitrex.gamification_service.service.quests.quest_generation.QuestGeneratorServiceFactory;
import de.unistuttgart.iste.meitrex.generated.dto.Quest;
import de.unistuttgart.iste.meitrex.generated.dto.QuestSet;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class QuestService implements IQuestService{

    private final IUserRepository userRepository;

    private final ICourseRepository courseRepository;

    private final AdaptivityConfiguration adaptivityConfiguration;

    private final QuestGeneratorServiceFactory questGeneratorServiceFactory;

    private final ModelMapper modelMapper;

    private final int DAILY_QUEST_COUNT = 3; // Number of quests in a daily quest set

    public QuestSet getDailyQuestSetForUser(final UUID courseId, final UUID userId) {
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

        return mapQuestSetEntityToDto(user, courseData.getDailyQuestSet());
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

        List<DailyQuestType> questTypeCandidates = new ArrayList<>(Arrays.stream(DailyQuestType.values()).toList());
        Collections.shuffle(questTypeCandidates);

        List<QuestEntity> quests = new ArrayList<>();
        while (quests.size() < DAILY_QUEST_COUNT) {
            if (questTypeCandidates.isEmpty())
                break;

            DailyQuestType questType = questTypeCandidates.removeLast();

            log.info("Attempting to generate quest of type {} for user {}", questType, user.getId());

            try {
                Optional<QuestEntity> generatedQuestEntity = questGeneratorServiceFactory.getQuestGenerator(questType)
                        .generateQuest(courseEntity, user, Collections.unmodifiableList(quests));

                generatedQuestEntity
                        .ifPresentOrElse(
                                quests::add,
                                () -> log.info("Could not generate a quest of type {} for user {}.", questType, user.getId())
                        );
            } catch (ContentServiceConnectionException e) {
                throw new RuntimeException(e);
            }
        }

        log.info("Generated {} quests for user {}", quests.size(), user.getId());

        LocalDate now = LocalDate.now();
        final QuestSetEntity questSetEntity = QuestSetEntity.builder()
                .name("Daily Quest Set for " + now)
                .course(courseEntity)
                .forDay(now)
                .quests(quests)
                .rewardMultiplier(rewardMultiplier)
                .build();

        generateUserGoalProgressEntitiesForQuestSet(user, questSetEntity);

        return questSetEntity;
    }

    private void generateUserGoalProgressEntitiesForQuestSet(UserEntity userEntity, QuestSetEntity questSetEntity) {
        UserCourseDataEntity courseData = userEntity.getCourseData(questSetEntity.getCourse().getId())
                .orElseThrow(() -> new IllegalArgumentException("No course data found for user " + userEntity.getId()));

        List<UserGoalProgressEntity> newGoalProgressEntities = questSetEntity.getQuests().stream()
                .map(qu -> qu.getGoal().generateUserGoalProgress(userEntity))
                .toList();

        courseData.getGoalProgressEntities().addAll(newGoalProgressEntities);
    }

    private QuestSet mapQuestSetEntityToDto(UserEntity user, QuestSetEntity questSetEntity) {
        final QuestSet questSet = modelMapper.map(questSetEntity, QuestSet.class);

        // we need to map the quest entities to DTOs manually because they have a completely different structure
        questSet.setQuests(questSetEntity.getQuests().stream().map(questEntity -> {
            UserGoalProgressEntity userGoalProgressEntity = user
                    .getCourseData(questEntity.getCourse().getId()).stream()
                    .flatMap(cd -> cd.getGoalProgressEntities().stream())
                    .filter(gp -> gp.getGoal().getId().equals(questEntity.getGoal().getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No goals found for user " + user.getId()));

            Quest quest = Quest.builder()
                    .setId(questEntity.getId())
                    .setName(questEntity.getName())
                    .setCourseId(questEntity.getCourse().getId())
                    .setUserId(user.getId())
                    .setTrackingStartTime(questEntity.getGoal().getTrackingStartTime())
                    .setTrackingEndTime(questEntity.getGoal().getTrackingEndTime())
                    .setCompleted(userGoalProgressEntity.isCompleted())
                    .setDescription(questEntity.getDescription())
                    .build();

            if(userGoalProgressEntity instanceof CountableUserGoalProgressEntity countableGoalProgress) {
                quest.setCompletedCount(countableGoalProgress.getCompletedCount());
                if(questEntity.getGoal() instanceof CountableGoalEntity countableGoal) {
                    quest.setRequiredCount(countableGoal.getRequiredCount());
                }
            }
            return quest;
        }).toList());

        return questSet;
    }
}
