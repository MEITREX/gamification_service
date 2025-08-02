package de.unistuttgart.iste.meitrex.gamification_service.model;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemInstanceEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ColorTheme extends ItemParent{
    private String foreColor;
    private String backColor;

    public ItemInstanceEntity toItemInstance() {
        ItemInstanceEntity itemInstanceEntity = new ItemInstanceEntity();
        itemInstanceEntity.setUniqueDescription("");
        itemInstanceEntity.setItemType(ItemType.ColorTheme);
        itemInstanceEntity.setEquipped(false);
        itemInstanceEntity.setPrototypeId(this.getId());
        return itemInstanceEntity;
    }
}
