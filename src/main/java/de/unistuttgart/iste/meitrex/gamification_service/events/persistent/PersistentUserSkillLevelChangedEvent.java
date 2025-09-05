package de.unistuttgart.iste.meitrex.gamification_service.events.persistent;

import de.unistuttgart.iste.meitrex.generated.dto.BloomLevel;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString (callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("USER_SKILL_LEVEL_CHANGED_EVENT")
public class PersistentUserSkillLevelChangedEvent extends PersistentEvent {

    @Column(name="fk_user_id")
    private UUID userId;

    @Column(name="fk_skill_id")
    private UUID skillId;

    @Column(name="bloom_level")
    @Enumerated(EnumType.STRING)
    private BloomLevel bloomLevel;

    @Column(name="new_value")
    private float newValue;

    @Column(name="old_value")
    private float oldValue;

}
