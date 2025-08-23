package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.skilllevels;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillEntity implements IWithId<UUID> {
    @Id
    private UUID id;

    @Nullable
    private String name;
}
