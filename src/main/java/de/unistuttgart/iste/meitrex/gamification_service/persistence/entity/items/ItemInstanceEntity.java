package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class ItemInstanceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    UUID prototypeId;

    @Column
    boolean equipped;

    @Column
    String uniqueDescription;
}
