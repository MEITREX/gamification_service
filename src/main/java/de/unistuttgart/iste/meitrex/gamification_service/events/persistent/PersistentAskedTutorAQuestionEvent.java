package de.unistuttgart.iste.meitrex.gamification_service.events.persistent;

import de.unistuttgart.iste.meitrex.common.event.TutorCategory;
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
@DiscriminatorValue("ASKED_TUTOR_A_QUESTION_EVENT")
public class PersistentAskedTutorAQuestionEvent extends PersistentEvent {
    private UUID userId;
    private UUID courseId;
    private String question;
    private TutorCategory category;
}
