package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Objects;

@Entity(name = "Achievement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AchievementEntity extends HasGoalEntity {
    @Column
    String name;

    @Column
    String imageUrl;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    CourseEntity course;

    public void setGoal(GoalEntity goal) {
        super.setGoal(goal);
        goal.setHasGoal(this);
    }



    @Override
    public String toString() {
        if (course == null) {
            return "AchievementEntity{" +
                    "super" + super.toString() +
                    ", name='" + name + '\'' +
                    ", imageUrl='" + imageUrl + '\'' +
                    ", course=null" +
                    '}';
        }
        return "AchievementEntity{" +
                "super="  + super.toString() +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", course=" + course.getId() +
                '}';
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AchievementEntity that = (AchievementEntity) o;
        if (course == null && that.course == null) {
            return Objects.equals(name, that.name) && Objects.equals(imageUrl, that.imageUrl);
        }
        if (course == null || that.course == null) {
            return false;
        }
        return Objects.equals(name, that.name) && Objects.equals(imageUrl, that.imageUrl)&& Objects.equals(course.getId(), that.course.getId());
    }

    @Override
    public int hashCode() {
        if (course == null) {
            return Objects.hash(super.hashCode(), name, imageUrl, null);
        }
        return Objects.hash(super.hashCode(), name, imageUrl, course.getId());
    }
}
