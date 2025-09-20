package de.unistuttgart.iste.meitrex.gamification_service.events.persistent;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@ToString (callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Entity
@DiscriminatorValue("USER_PROGRESS_UPDATED_EVENT")
public class PersistentUserProgressUpdatedEvent extends PersistentEvent {


    @Column(name="fk_user_id")
    private UUID userId;

    @Column(name="fk_content_id")
    private UUID contentId;

    @Column(name="fk_course_id")
    private UUID courseId;

    @Column(name="fk_chapter_id")
    private UUID chapterId;

    @Column(name="correctness")
    private Double correctness;

    @Column(name="assessment_attempt")
    @Builder.Default
    private int assessmentAttempt = 0;
}
