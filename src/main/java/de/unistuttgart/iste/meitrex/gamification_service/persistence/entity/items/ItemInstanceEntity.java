package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemInstanceEntity implements IWithId<UUID> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Setter(AccessLevel.NONE)
    OffsetDateTime creationTime;

    @Column(nullable = false)
    UUID prototypeId;

    @Column
    boolean equipped;

    @Column
    String uniqueDescription;

    @Column
    ItemType itemType;

    public ItemInstanceEntity() {
        creationTime = OffsetDateTime.now();
    }
}
