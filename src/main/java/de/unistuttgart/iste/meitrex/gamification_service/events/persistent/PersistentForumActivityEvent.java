package de.unistuttgart.iste.meitrex.gamification_service.events.persistent;

import de.unistuttgart.iste.meitrex.common.event.ForumActivity;
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
@DiscriminatorValue("FORUM_ACTIVITY_EVENT")
public class PersistentForumActivityEvent extends PersistentEvent {

    public enum Type {
        THREAD, QUESTION, ANSWER, INFO;
    }

    @Column(name="fk_user_id")
    private UUID userId;

    @Column(name="fk_forum_id")
    private UUID forumId;

    @Column(name="fk_course_id")
    private UUID courseId;

    @Enumerated(EnumType.STRING)
    @Column(name="type")
    private Type type;

}
