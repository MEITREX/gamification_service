package de.unistuttgart.iste.meitrex.gamification_service.controller;

import de.unistuttgart.iste.meitrex.gamification_service.service.PlayerHexadScoreService;
import de.unistuttgart.iste.meitrex.generated.dto.PlayerAnswerInput;
import de.unistuttgart.iste.meitrex.generated.dto.PlayerHexadScore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;


@Slf4j
@Controller
@RequiredArgsConstructor
public class PlayerHexadScoreController {

    private final PlayerHexadScoreService playerHexadScoreService;

    @MutationMapping
    public PlayerHexadScore evaluatePlayerHexadScore(@Argument UUID userId, @Argument PlayerAnswerInput input) {
        return playerHexadScoreService.evaluate(userId, input);
    }

    @QueryMapping
    public PlayerHexadScore getPlayerHexadScoreById(@Argument UUID userId) {
        return playerHexadScoreService.getById(userId);
    }

    @QueryMapping
    public Boolean PlayerHexadScoreExists(@Argument UUID userId) {
        return playerHexadScoreService.hasHexadScore(userId);
    }
}
