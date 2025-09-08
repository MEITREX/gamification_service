package de.unistuttgart.iste.meitrex.gamification_service.domain;

import de.unistuttgart.iste.meitrex.gamification_service.config.ItemConfiguration;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.UserInventoryEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserInventoryFactory {

    private final ItemConfiguration config;

    public UserInventoryEntity createUserInventory() {
        var e = new  UserInventoryEntity();
        e.setUnspentPoints(config.getDefaultStartingCapital());
        return e;
    }
}
