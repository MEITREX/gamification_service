package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.PlayerHexadScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper.PlayerHexadScoreMapper;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.PlayerHexadScoreRepository;
import de.unistuttgart.iste.meitrex.generated.dto.PlayerAnswerInput;
import de.unistuttgart.iste.meitrex.generated.dto.PlayerHexadScore;
import de.unistuttgart.iste.meitrex.generated.dto.PlayerType;
import de.unistuttgart.iste.meitrex.generated.dto.PlayerTypeScore;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional 
public class PlayerHexadScoreService {

    private final PlayerHexadScoreRepository playerHexadScoreRepository;
    private final PlayerHexadScoreMapper playerHexadScoreMapper;
     /**
     * Returns Player Hexad Types (%) according to quiz answers
     * @param input the players quiz answer
     * @param userId
     * @return the calculated player hexad score 
     */
    public PlayerHexadScore evaluate(UUID userId, PlayerAnswerInput input) {

        Optional<PlayerHexadScoreEntity> existingScore = playerHexadScoreRepository.findByUserId(userId);
        PlayerHexadScore playerHexadScore; 
        if(!existingScore.isPresent()){
            playerHexadScore = input.getQuestions().isEmpty() ? calculateDefault(): calculateFromInput(input);
            PlayerHexadScoreEntity newEntity = playerHexadScoreMapper.dtoToEntity(playerHexadScore.getScores(), userId);
            playerHexadScoreRepository.save(newEntity);
        } else {
            throw new IllegalStateException("Player Hexad Score was already evaluated");
        }
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

        return new PlayerHexadScore(scores);
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
            .map(question -> question.getSelectedAnswer())
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
                float normalizedScore = (max == 0) ? 0f: ((float) raw/max) * 100;
                return PlayerTypeScore.builder()
                    .setType(type)
                    .setValue(normalizedScore)
                    .build(); 
            }).collect(Collectors.toList());
        
        return new PlayerHexadScore(scores);
    }

     /**
     * Return users player hexad score 
     * @param userId
     * @return users player hexad score 
     */
    public PlayerHexadScore getById(UUID userId) {
        PlayerHexadScoreEntity entity = playerHexadScoreRepository.findByUserId(userId)
            .orElseThrow(() -> new EntityNotFoundException("No score found for user " + userId));
        return playerHexadScoreMapper.entityToDto(entity);
    }

    /**
     * Return wether the user has a player hexad score
     * @param userId
     * @return true if a hexad score exists otherwise false
     */
    public Boolean hasHexadScore(UUID userId) {
        Optional<PlayerHexadScoreEntity> entity = playerHexadScoreRepository.findByUserId(userId);
        if (entity.isPresent()) {
            return true;
        }
        return false;
    }
}
