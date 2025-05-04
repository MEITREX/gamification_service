package de.unistuttgart.iste.meitrex.gamification_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import de.unistuttgart.iste.meitrex.generated.dto.*;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import de.unistuttgart.iste.meitrex.gamification_service.service.PlayertypeService;


@Slf4j
@Controller
@RequiredArgsConstructor
public class PlayertypeController {

    private final PlayertypeService playerTypeService;
    @MutationMapping
    public PlayerHexadScore evaluatePlayerType(@Argument PlayerAnswerInput input) {
        return playerTypeService.evaluate(input);
    }


}
