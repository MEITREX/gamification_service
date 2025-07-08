package de.unistuttgart.iste.meitrex.gamification_service.events.persistent;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.stereotype.Component;

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

    public enum Status {
        RECEIVED, PROCESSED, FAILED_RETRY, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @Column(name="status", nullable = false)
    private Status status;

    @Column(name="attempt_count", nullable = false)
    private Integer attemptCount;

    @Column(name="max_count", nullable = false)
    private Integer maxCount;

    @Column(name="received_timestamp", nullable = false)
    private Long receivedTimestamp;

    @Column(name="last_processing_attempt_timestamp", nullable = false)
    private Long lastProcessingAttemptTimestamp;

}
