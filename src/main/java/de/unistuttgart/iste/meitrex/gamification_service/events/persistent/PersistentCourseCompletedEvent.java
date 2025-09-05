package de.unistuttgart.iste.meitrex.gamification_service.events.persistent;

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
@DiscriminatorValue("COURSE_COMPLETED_EVENT")
public class PersistentCourseCompletedEvent extends PersistentEvent {
    private UUID courseId;
    private UUID userId;
}
