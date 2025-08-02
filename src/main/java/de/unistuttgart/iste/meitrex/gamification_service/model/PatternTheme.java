package de.unistuttgart.iste.meitrex.gamification_service.model;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemInstanceEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
public class PatternTheme extends ItemParent{
    private String foreColor;
    private String name;
    private String description;
    private boolean obtainableInLottery;
    private boolean obtainableAsReward;
    private boolean obtainableInShop;
    private int moneyCost;
    private int sellCompensation;
    private String url;

    public ItemInstanceEntity toItemInstance() {
        ItemInstanceEntity itemInstanceEntity = new ItemInstanceEntity();
        itemInstanceEntity.setUniqueDescription("");
        itemInstanceEntity.setItemType(ItemType.PatternTheme);
        itemInstanceEntity.setEquipped(false);
        itemInstanceEntity.setPrototypeId(this.getId());
        return itemInstanceEntity;
    }
}
