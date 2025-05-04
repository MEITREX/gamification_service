package de.unistuttgart.iste.meitrex.gamification_service.service;

import java.util.Arrays;
import java.util.EnumMap;

import org.springframework.stereotype.Service;
import de.unistuttgart.iste.meitrex.generated.dto.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PlayertypeService {

     /**
     * Returns Player Hexad Types (%) according to quiz answers
     * @param input the players quiz answer
     * @return the calculated player hexad score 
     */
    public PlayerHexadScore evaluate(PlayerAnswerInput input) {
        if(input.getQuestions().isEmpty()) {
            return calculateDefaultPlayerHexadScore();
        } else {
            return calculatePlayerHexadScore(input);
        }
    }

    /**
     * Calculates Default Score (%) player hexad
     * Default Hexad Score (%) = 100 / number of types
     * @return the calculated default player hexad score
     */
    private PlayerHexadScore calculateDefaultPlayerHexadScore(){
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
    private PlayerHexadScore calculatePlayerHexadScore(PlayerAnswerInput input) {
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
}
