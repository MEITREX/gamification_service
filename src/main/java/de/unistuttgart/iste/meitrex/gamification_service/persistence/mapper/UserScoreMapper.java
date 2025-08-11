package de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserScoreMapper {

    private UserMapper userMapper;

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
