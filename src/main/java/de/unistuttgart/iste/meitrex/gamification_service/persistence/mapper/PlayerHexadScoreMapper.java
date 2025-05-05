package de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper;

import java.util.List;
import java.util.UUID;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.PlayerHexadScoreEntity;
import de.unistuttgart.iste.meitrex.generated.dto.PlayerTypeScore;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlayerHexadScoreMapper {
    public PlayerHexadScoreEntity dtoToEntity(List<PlayerTypeScore> scores, UUID userId) {
        PlayerHexadScoreEntity entity = new PlayerHexadScoreEntity();
        entity.setUserId(userId);
        for (PlayerTypeScore score : scores) {
            switch (score.getType()) {
                case PHILANTHROPIST -> entity.setPhilanthropist(score.getValue());
                case SOCIALISER    -> entity.setSocialiser(score.getValue());
                case FREE_SPIRIT   -> entity.setFreeSpirit(score.getValue());
                case ACHIEVER      -> entity.setAchiever(score.getValue());
                case PLAYER        -> entity.setPlayer(score.getValue());
                case DISRUPTOR     -> entity.setDisruptor(score.getValue());
            }
        }
        return entity;
    }
}
