package de.unistuttgart.iste.meitrex.gamification_service.model;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemInstanceEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemType;
import de.unistuttgart.iste.meitrex.generated.dto.UserItemComplete;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Tutor extends ItemParent{
    private String filename;
    private String nickname;
    private String url;

    public ItemInstanceEntity toItemInstance() {
        ItemInstanceEntity itemInstanceEntity = new ItemInstanceEntity();
        itemInstanceEntity.setUniqueDescription("");
        itemInstanceEntity.setItemType(ItemType.Tutor);
        itemInstanceEntity.setEquipped(false);
        itemInstanceEntity.setPrototypeId(this.getId());
        return itemInstanceEntity;
    }

    public UserItemComplete toCompleteUserItemInstance() {
        UserItemComplete userItemComplete = super.toCompleteUserItemInstance();
        userItemComplete.setUrl(this.getUrl());
        userItemComplete.setFilename(this.getFilename());
        userItemComplete.setNickname(this.getNickname());
        return userItemComplete;
    }
}
