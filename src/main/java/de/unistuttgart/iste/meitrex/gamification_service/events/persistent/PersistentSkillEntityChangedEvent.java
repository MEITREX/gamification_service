package de.unistuttgart.iste.meitrex.gamification_service.events.persistent;

import de.unistuttgart.iste.meitrex.common.event.CrudOperation;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString (callSuper = true)
@Entity
@EqualsAndHashCode(callSuper = true)
@DiscriminatorValue("SKILL_ENTITY_CHANGED_EVENT")
public class PersistentSkillEntityChangedEvent extends PersistentEvent {


    @Column(name="fk_skill_id")
    private UUID skillId;

    @Column(name="skill_name")
    private String skillName;

    @Column(name="skill_category")
    private String skillCategory;

    @Column(name="crud_operation")
    @Enumerated(EnumType.STRING)
    private CrudOperation operation;

}
