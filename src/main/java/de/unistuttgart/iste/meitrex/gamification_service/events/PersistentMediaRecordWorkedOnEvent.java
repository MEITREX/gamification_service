package de.unistuttgart.iste.meitrex.gamification_service.events;

import de.unistuttgart.iste.meitrex.common.event.MediaRecordInfoEvent;
import de.unistuttgart.iste.meitrex.common.event.MediaRecordWorkedOnEvent;
import de.unistuttgart.iste.meitrex.common.event.MediaType;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;
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
@DiscriminatorValue("MEDIA_RECORD_WORKED_ON_EVENT")
public class PersistentMediaRecordWorkedOnEvent extends PersistentEvent {

    @Column(name="media_record_id")
    private UUID mediaRecordId;

    @Column(name="user_id")
    private UUID userId;

}
