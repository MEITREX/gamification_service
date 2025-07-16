package de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.PlayerHexadScoreEntity;
import de.unistuttgart.iste.meitrex.generated.dto.PlayerHexadScore;
import de.unistuttgart.iste.meitrex.generated.dto.PlayerType;
import de.unistuttgart.iste.meitrex.generated.dto.PlayerTypeScore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

     public PlayerHexadScore entityToDto(PlayerHexadScoreEntity entity) {
        List<PlayerTypeScore> scores = new ArrayList<>();

        scores.add(new PlayerTypeScore(PlayerType.PHILANTHROPIST, entity.getPhilanthropist()));
        scores.add(new PlayerTypeScore(PlayerType.SOCIALISER, entity.getSocialiser()));
        scores.add(new PlayerTypeScore(PlayerType.FREE_SPIRIT, entity.getFreeSpirit()));
        scores.add(new PlayerTypeScore(PlayerType.ACHIEVER, entity.getAchiever()));
        scores.add(new PlayerTypeScore(PlayerType.PLAYER, entity.getPlayer()));
        scores.add(new PlayerTypeScore(PlayerType.DISRUPTOR, entity.getDisruptor()));

        return new PlayerHexadScore(scores);
    }
}
