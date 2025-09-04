package de.unistuttgart.iste.meitrex.gamification_service.events.persistent;

import de.unistuttgart.iste.meitrex.common.event.MediaRecordInfoEvent;
import de.unistuttgart.iste.meitrex.common.event.MediaType;
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
@DiscriminatorValue("MEDIA_RECORD_INFO_EVENT")
public class PersistentMediaRecordInfoEvent extends PersistentEvent {

    @Column(name="media_record_id")
    private UUID mediaRecordId;

    @Column(name="duration_in_seconds")
    private Float durationInSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name="media_type")
    private MediaType mediaType;

    @Column(name="page_count")
    private Integer pageCount;

}
