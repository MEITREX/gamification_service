package de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper;

import de.unistuttgart.iste.meitrex.gamification_service.aspects.logging.Loggable;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.*;

@Component
@RequiredArgsConstructor
public class UserScoreMapper {

    private final UserMapper userMapper;

    public UserScore toDTO(UserScoreEntity userScoreEntity, int maxDepth) {
        if(userScoreEntity == null || maxDepth < 0) {
            return null;
        }
        final UserScore userScore = new UserScore();
        userScore.setId(MappingUtility.toString(userScoreEntity.getId()));
        userScore.setScore(userScoreEntity.getScore());
        userScore.setUser(this.userMapper.toDTO(userScoreEntity.getUser(), maxDepth - 1));
        return userScore;
    }

}
