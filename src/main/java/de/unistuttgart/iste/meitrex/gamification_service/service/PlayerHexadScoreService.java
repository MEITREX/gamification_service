package de.unistuttgart.iste.meitrex.gamification_service.service;

import java.util.*;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.PlayerHexadScoreQuestionEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IPlayerHexadScoreQuestionRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import org.springframework.stereotype.Service;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.PlayerHexadScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper.PlayerHexadScoreMapper;
import de.unistuttgart.iste.meitrex.generated.dto.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import lombok.*;

import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional 
public class PlayerHexadScoreService implements IPlayerHexadScoreService {

    private final PlayerHexadScoreMapper playerHexadScoreMapper;

    private final IUserCreator userCreator;

    private final IPlayerHexadScoreQuestionRepository playerHexadScoreQuestionRepository;


    /*Modified Review Required*/

     /**
     * Returns Player Hexad Types (%) according to quiz answers
     * @param input the players quiz answer
     * @param userId
     * @param username name of the user
     * @return the calculated player hexad score 
     */
    public PlayerHexadScore evaluate(UUID userId, PlayerAnswerInput input, String username) {
        final UserEntity user = userCreator.fetchOrCreate(userId);
        PlayerHexadScoreEntity playerHexadScoreEntity = user.getPlayerHexadScore();
        if(playerHexadScoreEntity != null && !playerHexadScoreEntity.isDefaultInput()) {
            throw new IllegalStateException("Player Hexad Score was already evaluated");
        }
        final PlayerHexadScore playerHexadScore = input.getQuestions().isEmpty() ? calculateDefault(): calculateFromInput(input);
        input.getQuestions().forEach(question -> {
            PlayerHexadScoreQuestionEntity playerHexadScoreQuestionEntity = new PlayerHexadScoreQuestionEntity();
            playerHexadScoreQuestionEntity.setQuestion(question.getText());
            playerHexadScoreQuestionEntity.setAnswer(question.getSelectedAnswer().getText());
            playerHexadScoreQuestionEntity.setUsername(username);
            playerHexadScoreQuestionRepository.save(playerHexadScoreQuestionEntity);
        });
        playerHexadScoreEntity = playerHexadScoreMapper.dtoToEntity(playerHexadScore.getScores(), userId);
        log.info("is default input: {}",playerHexadScoreEntity.isDefaultInput());
        playerHexadScoreEntity.setUsername(username);
        user.setPlayerHexadScore(playerHexadScoreEntity);
        playerHexadScoreEntity.setUser(user);
        return playerHexadScore;
    }

    /**
     * Calculates Default Score (%) player hexad
     * Default Hexad Score (%) = 100 / number of types
     * @return the calculated default player hexad score
     */
    public PlayerHexadScore calculateDefault(){
        float defaultValue = 100f / PlayerType.values().length;

        List<PlayerTypeScore> scores = Arrays.stream(PlayerType.values())
        .map(type -> PlayerTypeScore.builder()   
            .setType(type)                         
            .setValue(defaultValue)                  
            .build())                           
        .collect(Collectors.toList());
        return new PlayerHexadScore(true, scores);
    }

    /**
     * Calculates the Normalized Score (%) player hexad 
     * Normalized Score (%) = (Raw Score / Maximum Possible Score) × 100.
     * Raw Score: For each selected option, add 1 point to all Hexad types listed under that option.
     * Maximum Possible Score: Total number of questions the type appears in
     * @param input of the quiz answers
     * @return the calculated player hexad score
     */
    public PlayerHexadScore calculateFromInput(PlayerAnswerInput input) {
        Map<PlayerType, Integer> rawScores = new EnumMap<>(PlayerType.class);
        Map<PlayerType, Integer> maxPossibleScores = new EnumMap<>(PlayerType.class);

        // Init maps
        Arrays.stream(PlayerType.values()).forEach(type -> {
            rawScores.put(type, 0);
            maxPossibleScores.put(type, 0);
            }
        );

        // Raw score
        input.getQuestions().stream()
            .map(QuestionInput::getSelectedAnswer)
            .flatMap(answer -> answer.getPlayerTypes().stream())
            .forEach(type ->  rawScores.put(type, rawScores.get(type) + 1));

        // Maximum possible score
        input.getQuestions().stream()
            .flatMap(question -> question.getPossibleAnswers().stream())
            .flatMap(answer -> answer.getPlayerTypes().stream())
            .forEach(type -> maxPossibleScores.put(type, maxPossibleScores.get(type) + 1));
        
        // Normalized score (%)
        List<PlayerTypeScore> scores = Arrays.stream(PlayerType.values())
            .map(type -> {
                float raw = rawScores.get(type);
                int max = maxPossibleScores.get(type);
                float normalizedScore = (max == 0) ? 0f: (raw /max) * 100;
                return PlayerTypeScore.builder()
                    .setType(type)
                    .setValue(normalizedScore)
                    .build(); 
            }).collect(Collectors.toList());
        
        return new PlayerHexadScore(false, scores);
    }

     /**
     * Return users player hexad score 
     * @param userId
     * @return users player hexad score 
     */
    public PlayerHexadScore getById(UUID userId) {
        UserEntity user = userCreator.fetchOrCreate(userId);
        return playerHexadScoreMapper.entityToDto(user.getPlayerHexadScore());
    }

    /*Modified Review Required*/

    /**
     * Return wether the user has a player hexad score
     * @param userId
     * @return true if a hexad score exists otherwise false
     */
    public Boolean hasHexadScore(UUID userId) {
        UserEntity user = userCreator.fetchOrCreate(userId);
        return user.getPlayerHexadScore() != null;
    }


}
