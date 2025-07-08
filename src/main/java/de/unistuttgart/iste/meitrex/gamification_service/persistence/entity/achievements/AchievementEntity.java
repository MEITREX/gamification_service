package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Objects;
import java.util.UUID;

@Entity(name = "Achievement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AchievementEntity implements IWithId<UUID> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column
    String name;

    @Column
    String imageUrl;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    GoalEntity goal;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    CourseEntity course;

    public void setGoal(GoalEntity goal) {
        this.goal = goal;
        goal.setAchievement(this);
    }



    @Override
    public String toString() {
        if (course == null) {
            return "AchievementEntity{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", imageUrl='" + imageUrl + '\'' +
                    ", goal=" + goal +
                    ", course=null" +
                    '}';
        }
        return "AchievementEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", goal=" + goal +
                ", course=" + course.getId() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AchievementEntity that = (AchievementEntity) o;
        if (course == null && that.course == null) {
            return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(imageUrl, that.imageUrl) && Objects.equals(goal, that.goal);
        }
        if (course == null || that.course == null) {
            return false;
        }
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(imageUrl, that.imageUrl) && Objects.equals(goal, that.goal) && Objects.equals(course.getId(), that.course.getId());
    }

    @Override
    public int hashCode() {
        if (course == null) {
            return Objects.hash(id, name, imageUrl, goal, null);
        }
        return Objects.hash(id, name, imageUrl, goal, course.getId());
    }
}
