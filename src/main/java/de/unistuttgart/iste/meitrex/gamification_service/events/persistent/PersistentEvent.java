package de.unistuttgart.iste.meitrex.gamification_service.events.persistent;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class PersistentEvent {

    @Getter
    @Setter
    @SuperBuilder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    @EqualsAndHashCode
    @Entity
    @IdClass(PersistentEventStatusID.class)
    public static class PersistentEventStatus {

        public enum Status {
            RECEIVED, PROCESSED, FAILED_RETRY, FAILED
        }

        @Id
        @ManyToOne
        @JoinColumn(name="fk_persistent_event", referencedColumnName = "id")
        private PersistentEvent persistentEvent;

        @Id
        @Column(name="internal_event_listener_id")
        private UUID internalEventListenerId;

        @Column(name="cur_status", nullable = false)
        private Status status;

        @Column(name="cur_attempt", nullable = false)
        private Integer curAttempt;

        @Column(name="max_attempt_count", nullable = false)
        private Integer maxAttemptCount;

        @Column(name="last_processing_attempt_timestamp", nullable = false)
        private Long lastProcessingAttemptTimestamp;

    }

    @Setter
    @Getter
    @ToString
    @EqualsAndHashCode
    public static class PersistentEventStatusID implements Serializable {

        private UUID persistentEvent;

        private UUID internalEventListenerId;

        public PersistentEventStatusID() {}

        public PersistentEventStatusID(UUID persistentEvent, UUID internalEventListenerId) {
            this.persistentEvent = persistentEvent;
            this.internalEventListenerId = internalEventListenerId;
        }

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private UUID uuid;

    @Column(name="msg_sequence_no", unique = true)
    private Long msgSequenceNo;

    @Column(name="received_timestamp", nullable = false)
    private Long receivedTimestamp;

    @OneToMany(mappedBy = "persistentEvent")
    @Builder.Default
    private List<PersistentEventStatus> persistentEventStatusList = new ArrayList<>();

}
