package de.unistuttgart.iste.meitrex.gamification_service.service;


import java.util.*;

import de.unistuttgart.iste.meitrex.gamification_service.model.ItemParent;
import de.unistuttgart.iste.meitrex.generated.dto.*;

public interface IItemService {

    /**
     * Get the full inventory for a user, initializing defaults if needed.
     *
     */
    Inventory getInventoryForUser(UUID userId);

    /**
     * Get the list of items for a user, initializing defaults if needed.
     *
     */
    List<UserItem> getItemsForUser(UUID userId);

    /**
     * Get the list of inventories for the given users with a 0 for the Unspent points, initializing defaults if needed.
     * This query is meant for use cases where you need the items of other users
     */
    List<Inventory> getInventoriesForUsers(List<UUID> userIds);

    /**
     * Get the list of equipped items for the given users.
     */
    List<EquippedItems> getEquippedItemsForUsers(List<UUID> userIds);

    /**
     * Purchase an item for a user (deducts points if affordable) and return updated inventory.
     *
     */
    Inventory buyItem(UUID userId, UUID itemId);

    /**
     * Equip a specific item for a user (unequips conflicting items) and return updated inventory.
     *
     */
    Inventory equipItem(UUID userId, UUID itemId);

    /**
     * Unequip a specific item for a user (except Tutor type) and return updated inventory.
     *
     */
    Inventory unequipItem(UUID userId, UUID itemId);

    /**
     * Add a reward item to a user; if duplicate, grant sell compensation. Returns updated inventory.
     *
     */
    Inventory addItemRewardToUser(UUID userId, UUID itemId);

    /**
     * Add unspent points to a user's inventory and return updated inventory.
     *
     */
    Inventory addPointsToUser(UUID userId, Integer points);

    /**
     * Perform a lottery roll for the user. Deducts cost and returns the awarded item,
     * or null if the user lacks sufficient points.
     *
     */
    UserItemComplete lotteryRun(UUID userId);

    /**
     * Get the item prototype by its ID.
     */
    Optional<ItemParent> getItemPrototypeById(UUID itemId);
}