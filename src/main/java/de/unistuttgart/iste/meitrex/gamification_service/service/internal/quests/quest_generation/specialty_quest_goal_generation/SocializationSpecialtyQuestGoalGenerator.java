package de.unistuttgart.iste.meitrex.gamification_service.service.internal.quests.quest_generation.specialty_quest_goal_generation;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.LeaderboardEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.MoveUpLeaderboardGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ILeaderboardRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.ILeaderboardService;
import de.unistuttgart.iste.meitrex.gamification_service.service.reactive.leaderboard.UserProgressUpdatedLeaderboardListener;
import de.unistuttgart.iste.meitrex.gamification_service.time.Period;
import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocializationSpecialtyQuestGoalGenerator implements ISpecialtyQuestGoalGenerator {
    private final ILeaderboardRepository leaderboardRepository;

    @Override
    public GamificationCategory getCategory() {
        return GamificationCategory.SOCIALIZATION;
    }

    @Override
    public Optional<GoalEntity> generateGoal(UserEntity user, CourseEntity course) {
        if(isUserTop1InAllLeaderboards(course, user)) {
            log.info("User {} is already top 1 in all leaderboards of course {}, not generating socialization quest",
                    user.getId(), course.getId());
            return Optional.empty();
        }

        MoveUpLeaderboardGoalEntity goal = new MoveUpLeaderboardGoalEntity();
        goal.setTrackingTimeToToday();
        return Optional.of(goal);
    }

    @Override
    public String getQuestTitle() {
        return "Socialization Quest";
    }

    private boolean isUserTop1InAllLeaderboards(CourseEntity course, UserEntity user) {
        Optional<LeaderboardEntity> allTimeLeaderboard =
                leaderboardRepository.findByCourseAndPeriodOrderByStartDateDesc(course, Period.ALL_TIME);
        Optional<LeaderboardEntity> monthlyLeaderboard =
                leaderboardRepository.findByCourseAndPeriodOrderByStartDateDesc(course, Period.MONTHLY);
        Optional<LeaderboardEntity> weeklyLeaderboard =
                leaderboardRepository.findByCourseAndPeriodOrderByStartDateDesc(course, Period.WEEKLY);
        OptionalInt maxRank = Stream.concat(
                Stream.concat(allTimeLeaderboard.stream(), monthlyLeaderboard.stream()), weeklyLeaderboard.stream())
                .map(leaderboard ->
                        UserProgressUpdatedLeaderboardListener.computeRank(leaderboard.getScoreEntityList(), user.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .mapToInt(x -> x)
                .max();

        if(maxRank.isEmpty())
            return false;

        return maxRank.getAsInt() == 1;
    }
}
