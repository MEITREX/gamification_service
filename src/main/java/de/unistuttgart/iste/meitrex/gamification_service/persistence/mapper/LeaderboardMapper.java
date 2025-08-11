package de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper;


import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.LeaderboardEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.time.Period;
import de.unistuttgart.iste.meitrex.generated.dto.Leaderboard;
import de.unistuttgart.iste.meitrex.generated.dto.UserScore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class LeaderboardMapper {

    private final UserScoreMapper userScoreMapper;

    public Leaderboard toDTO(LeaderboardEntity leaderboardEntity, int maxDepth) {
        if(leaderboardEntity == null || maxDepth < 0) {
            return null;
        }

        final Leaderboard leaderboard = new Leaderboard();
        leaderboard.setId(MappingUtility.toString(leaderboardEntity.getId()));
        leaderboard.setTitle(leaderboardEntity.getTitle());
        leaderboard.setStartDate(leaderboardEntity.getStartDate());
        leaderboard.setUserScores(toDTO(leaderboardEntity.getScoreEntityList(), maxDepth));
        final Period period = leaderboardEntity.getPeriod();
        if(Objects.nonNull(period)) {
            leaderboard.setPeriod(de.unistuttgart.iste.meitrex.generated.dto.Period.valueOf(period.toString()));
        }
        return leaderboard;
    }

    private List<UserScore> toDTO(List<UserScoreEntity> userScoreEntityList, int maxDepth) {
        return MappingUtility.nullToEmptyList(userScoreEntityList)
                .stream()
                .map(score -> this.userScoreMapper.toDTO(score, maxDepth -1))
                .filter(Objects::nonNull)
                .toList();
    }

}
