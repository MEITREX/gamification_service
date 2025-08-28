package de.unistuttgart.iste.meitrex.gamification_service.controller;

import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.gamification_service.service.quests.QuestService;
import de.unistuttgart.iste.meitrex.generated.dto.QuestSet;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@Slf4j
@RequiredArgsConstructor
public class QuestController {
    private final QuestService questService;

    @QueryMapping("_internal_noauth_currentUserDailyQuestSet")
    private QuestSet currentUserDailyQuestSet(@NotNull @Argument final UUID courseId,
                                              @ContextValue final LoggedInUser currentUser) {
        return questService.getDailyQuestSetForUser(courseId, currentUser.getId());
    }
}
