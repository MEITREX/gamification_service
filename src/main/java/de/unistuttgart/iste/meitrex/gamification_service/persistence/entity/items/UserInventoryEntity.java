package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserInventoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column
    int unspentPoints;

    @OneToMany(cascade = CascadeType.ALL)
    List<ItemInstanceEntity> items;

    public UserInventoryEntity() {
        unspentPoints = 0;
        items = new ArrayList<>();
    }

    public void addPoints(int points) {
        unspentPoints += points;
    }

    public void removePoints(int points) {
        unspentPoints -= points;
    }
}
