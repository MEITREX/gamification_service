package de.unistuttgart.iste.meitrex.gamification_service.controller;

import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.gamification_service.service.ItemService;
import de.unistuttgart.iste.meitrex.generated.dto.Inventory;
import de.unistuttgart.iste.meitrex.generated.dto.UserItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @QueryMapping
    public List<UserItem> itemsByUserId(@Argument UUID userId,
                                        @ContextValue final LoggedInUser loggedInUser) {
        return itemService.getItemsForUser(userId);
    }

    @QueryMapping
    public Inventory inventoryForUser(@ContextValue final LoggedInUser loggedInUser) {
       return itemService.getInventoryForUser(loggedInUser.getId());
    }

    @MutationMapping
    public Inventory buyItem(@Argument UUID itemId,
                             @ContextValue final LoggedInUser loggedInUser) {
        return new Inventory();
    }
}
