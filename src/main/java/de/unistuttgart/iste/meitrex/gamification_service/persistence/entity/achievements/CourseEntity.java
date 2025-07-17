package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.generated.dto.Chapter;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "Course")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CourseEntity implements IWithId<UUID> {
    @Id
    UUID id;

    @ElementCollection
    List<Chapter> chapters;

    @OneToMany(cascade = CascadeType.ALL)
    List<AchievementEntity> achievements;

    public CourseEntity(final UUID id, final List<Chapter> chapters) {
        this.id = id;
        this.chapters = chapters;
        this.achievements = new ArrayList<>();
    }
}
