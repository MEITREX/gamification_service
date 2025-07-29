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
        UserItem item1 = UserItem.builder()
                .setEquipped(true)
                .setId(UUID.fromString("1f1c362f-ff7c-490a-82fa-e478510bcf41"))
                .setUnlocked(true)
                .setUniqueDescription("Unique description")
                .build();
        UserItem item2 = UserItem.builder()
                .setEquipped(false)
                .setId(UUID.fromString("6fb8b726-2db2-4992-9e63-f3aa57aa4520"))
                .setUnlocked(false)
                .setUniqueDescription("Unique description two")
                .build();
        UserItem item3 = UserItem.builder()
                .setEquipped(false)
                .setId(itemId)
                .setUnlocked(true)
                .setUniqueDescription("Unique description bought")
                .build();
        return Inventory.builder()
                .setUserId(loggedInUser.getId())
                .setItems(List.of(item1, item2, item3))
                .setUnspentPoints(30)
                .build();
    }
}
