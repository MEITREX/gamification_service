package de.unistuttgart.iste.meitrex.gamification_service.model;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemInstanceEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemType;
import de.unistuttgart.iste.meitrex.generated.dto.UserItemComplete;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProfilePic extends ItemParent{
    private String url;

    public ItemInstanceEntity toItemInstance() {
        ItemInstanceEntity itemInstanceEntity = new ItemInstanceEntity();
        itemInstanceEntity.setUniqueDescription("");
        itemInstanceEntity.setItemType(ItemType.ProfilePic);
        itemInstanceEntity.setEquipped(false);
        itemInstanceEntity.setPrototypeId(this.getId());
        return itemInstanceEntity;
    }
    public UserItemComplete toCompleteUserItemInstance() {
        UserItemComplete userItemComplete = super.toCompleteUserItemInstance();
        userItemComplete.setUrl(this.getUrl());
        return userItemComplete;
    }
}
