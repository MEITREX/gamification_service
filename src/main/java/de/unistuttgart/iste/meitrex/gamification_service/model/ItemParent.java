package de.unistuttgart.iste.meitrex.gamification_service.model;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemInstanceEntity;
import lombok.Data;

import java.util.UUID;

@Data
public abstract class ItemParent {
    private String name;
    private String description;
    private UUID id;
    private ItemRarity rarity;
    private int moneyCost;
    private int sellCompensation;
    private boolean obtainableInLottery;
    private boolean obtainableAsReward;
    private boolean obtainableInShop;

    public abstract ItemInstanceEntity toItemInstance();
}
