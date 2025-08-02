package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.gamification_service.model.ColorTheme;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class ItemInstanceEntity implements IWithId<UUID> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column(nullable = false)
    UUID prototypeId;

    @Column
    boolean equipped;

    @Column
    String uniqueDescription;

    @Column
    ItemType itemType;
}
