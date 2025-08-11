package de.unistuttgart.iste.meitrex.gamification_service.events.persistent;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
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
@DiscriminatorValue("USER_PROGRESS_UPDATED")
public class PersistentUserProgressUpdatedEvent extends PersistentEvent {


    @Column(name="fk_user_id", nullable = false)
    private UUID userId;

    @Column(name="fk_content_id", nullable = false)
    private UUID contentId;

    @Column(name="fk_course_id", nullable = false)
    private UUID courseId;

    @Column(name="correctness", nullable = false)
    private Double correctness;

    @Column(name="assessment_attempt", nullable = false)
    private int assessmentAttempt = 0;
}
