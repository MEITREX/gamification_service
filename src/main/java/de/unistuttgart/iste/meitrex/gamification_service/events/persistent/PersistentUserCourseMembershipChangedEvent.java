package de.unistuttgart.iste.meitrex.gamification_service.events.persistent;

import de.unistuttgart.iste.meitrex.generated.dto.UserRoleInCourse;
import jakarta.annotation.Nullable;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
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
@DiscriminatorValue("COURSE_MEMBERSHIP_CHANGED_EVENT")
public class PersistentUserCourseMembershipChangedEvent extends PersistentEvent {
    @NotNull
    private UUID userId;

    @NotNull
    private UUID courseId;

    /**
     * The previous role of the user in the course, or null if the user was not a member of the course before.
     */
    @Nullable
    private UserRoleInCourse previousRole;

    /**
     * The new role of the user in the course, or null if the user is no longer a member of the course.
     */
    @Nullable
    private UserRoleInCourse newRole;
}
