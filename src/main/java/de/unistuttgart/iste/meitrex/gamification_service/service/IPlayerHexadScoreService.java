package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.PlayerHexadScoreEntity;
import de.unistuttgart.iste.meitrex.generated.dto.PlayerAnswerInput;
import de.unistuttgart.iste.meitrex.generated.dto.PlayerHexadScore;
import de.unistuttgart.iste.meitrex.generated.dto.PlayerType;
import de.unistuttgart.iste.meitrex.generated.dto.PlayerTypeScore;
import jakarta.persistence.EntityNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

public interface IPlayerHexadScoreService {

    /**
     * Returns Player Hexad Types (%) according to quiz answers
     * @param input the players quiz answer
     * @param userId
     * @param username
     * @return the calculated player hexad score
     */
    PlayerHexadScore evaluate(UUID userId, PlayerAnswerInput input, String username);

    /**
     * Calculates Default Score (%) player hexad
     * Default Hexad Score (%) = 100 / number of types
     * @return the calculated default player hexad score
     */
    PlayerHexadScore calculateDefault(UUID userId);

    /**
     * Calculates the Normalized Score (%) player hexad
     * Normalized Score (%) = (Raw Score / Maximum Possible Score) × 100.
     * Raw Score: For each selected option, add 1 point to all Hexad types listed under that option.
     * Maximum Possible Score: Total number of questions the type appears in
     * @param input of the quiz answers
     * @return the calculated player hexad score
     */
    PlayerHexadScore calculateFromInput(PlayerAnswerInput input);

    /**
     * Return users player hexad score
     * @param userId
     * @return users player hexad score
     */
     PlayerHexadScore getById(UUID userId);

    /**
     * Return wether the user has a player hexad score
     * @param userId
     * @return true if a hexad score exists otherwise false
     */
    Boolean hasHexadScore(UUID userId);
}
