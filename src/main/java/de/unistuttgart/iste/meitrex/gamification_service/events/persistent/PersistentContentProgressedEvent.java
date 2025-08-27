package de.unistuttgart.iste.meitrex.gamification_service.events.persistent;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString (callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("CONTENT_PROGRESSED_EVENT")
public class PersistentContentProgressedEvent extends PersistentEvent {

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString (callSuper = true)
    @Entity
    @DiscriminatorValue("PERSISTENT_RESPONSE")
    public static class PersistentResponse {

        @Id
        @GeneratedValue
        @Column(name="response_id")
        private UUID responseId;

        @Column(name="fk_item_id")
        private UUID itemId;

        @Column(name="response")
        private float response;

        @ManyToOne
        @JoinColumn(name="fk_content_progressed_event")
        private PersistentContentProgressedEvent event;
    }

    @Column(name="fk_user_id")
    private UUID userId;

    @Column(name="fk_content_id")
    private UUID contentId;

    @Column(name="success")
    private boolean success;

    @Column(name="correctness")
    private double correctness;

    @Column(name="hints_user")
    private int hintsUsed;

    @Column(name="time_to_complete")
    private Integer timeToComplete;

    @OneToMany(mappedBy = "event")
    private List<PersistentResponse> responses = new ArrayList<>();

}
