package de.unistuttgart.iste.meitrex.gamification_service.controller;

import de.unistuttgart.iste.meitrex.gamification_service.service.IUserService;
import de.unistuttgart.iste.meitrex.generated.dto.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Controller
public class UserController {

    private final IUserService userService;

    @QueryMapping
    public List<User> getUser(@Argument UUID userID) {
        return List.of(userService.fetchUser(userID));
    }
}
