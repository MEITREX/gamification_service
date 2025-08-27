package de.unistuttgart.iste.meitrex.gamification_service.controller;

import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.gamification_service.service.IItemService;
import de.unistuttgart.iste.meitrex.generated.dto.Inventory;
import de.unistuttgart.iste.meitrex.generated.dto.UserItem;
import de.unistuttgart.iste.meitrex.generated.dto.UserItemComplete;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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

    private final IItemService itemService;

    @QueryMapping
    public List<UserItem> itemsByUserId(@Argument UUID userId,
                                        @ContextValue final LoggedInUser currentUser) {
        return itemService.getItemsForUser(userId);
    }

    @QueryMapping
    public Inventory inventoryForUser(@NotNull @ContextValue final LoggedInUser currentUser) {
        return itemService.getInventoryForUser(currentUser.getId());
    }

    @MutationMapping
    public Inventory buyItem(@Argument UUID itemId,
                             @ContextValue final LoggedInUser currentUser) {
        return itemService.buyItem(currentUser.getId(), itemId);
    }

    @MutationMapping
    public Inventory equipItem(@Argument UUID itemId,
                               @ContextValue final LoggedInUser currentUser) {
        return itemService.equipItem(currentUser.getId(), itemId);
    }

    @MutationMapping
    public Inventory unequipItem(@Argument UUID itemId,
                               @ContextValue final LoggedInUser currentUser) {
        return itemService.unequipItem(currentUser.getId(), itemId);
    }

    @MutationMapping
    public Inventory itemReward(@Argument UUID itemId,
                                @ContextValue final LoggedInUser currentUser) {
        return itemService.addItemRewardToUser(currentUser.getId(), itemId);
    }

    @MutationMapping
    public Inventory currencyReward(@Argument Integer points,
                                    @ContextValue final LoggedInUser currentUser) {
        return itemService.addPointsToUser(currentUser.getId(), points);
    }

    @MutationMapping
    public UserItemComplete lotteryRun(@ContextValue final LoggedInUser currentUser) {
        return itemService.lotteryRun(currentUser.getId());
    }
}
