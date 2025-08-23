package de.unistuttgart.iste.meitrex.gamification_service.model;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.ItemInstanceEntity;
import de.unistuttgart.iste.meitrex.generated.dto.UserItemComplete;
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

    public UserItemComplete toCompleteUserItemInstance() {
        UserItemComplete userItemComplete = new UserItemComplete();
        userItemComplete.setName(this.getName());
        userItemComplete.setDescription(this.getDescription());
        userItemComplete.setId(this.getId());
        userItemComplete.setRarity(this.getRarity().toString());
        userItemComplete.setMoneyCost(this.getMoneyCost());
        userItemComplete.setSellCompensation(this.getSellCompensation());
        userItemComplete.setObtainableInLottery(this.isObtainableInLottery());
        userItemComplete.setObtainableAsReward(this.isObtainableAsReward());
        userItemComplete.setObtainableInShop(this.isObtainableInShop());
        return userItemComplete;
    }
}
