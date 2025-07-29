package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
public class UserInventoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column
    int unspentPoints;

    @OneToMany
    List<ItemInstanceEntity> items;

    public UserInventoryEntity() {
        unspentPoints = 0;
        items = new ArrayList<>();
    }
}
