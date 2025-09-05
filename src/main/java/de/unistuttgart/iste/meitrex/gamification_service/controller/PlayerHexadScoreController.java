package de.unistuttgart.iste.meitrex.gamification_service.controller;

import de.unistuttgart.iste.meitrex.gamification_service.service.IPlayerHexadScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.unistuttgart.iste.meitrex.generated.dto.*;

import java.util.UUID;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import de.unistuttgart.iste.meitrex.gamification_service.service.PlayerHexadScoreService;


@Slf4j
@Controller
@RequiredArgsConstructor
public class PlayerHexadScoreController {

    /*Modified Review Required*/

    private final IPlayerHexadScoreService playerHexadScoreService;

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
