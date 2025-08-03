package de.unistuttgart.iste.meitrex.gamification_service.model;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemInstanceEntity;
import lombok.Data;

import java.util.UUID;

@Data
public abstract class ItemParent {
    private UUID id;
    private ItemRarity rarity;

    public abstract ItemInstanceEntity toItemInstance();
}
