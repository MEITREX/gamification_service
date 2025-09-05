package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity(name = "HasGoal")
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class HasGoalEntity implements IWithId<UUID> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private GoalEntity goal;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private CourseEntity course;

    @Override
    public String toString() {
        return "HasGoalEntity{" +
                "id=" + id +
                ", goal=" + goal +
                ", course=" + course.getId() +
                '}';
    }
}
