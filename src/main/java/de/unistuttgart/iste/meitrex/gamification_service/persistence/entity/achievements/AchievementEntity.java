package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    /**
     * If true, this is an adaptive achievement, which means that not all users will receive it. Only if a user has
     * a UserGoalProgressEntity for this achievement, they can progress on it.
     */
    @Column
    boolean adaptive;

    public void setGoal(GoalEntity goal) {
        super.setGoal(goal);
        goal.setParentWithGoal(this);
    }

    @Override
    public String toString() {
        if (getCourse() == null) {
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
                ", course=" + getCourse().getId() +
                '}';
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        AchievementEntity that = (AchievementEntity) o;
        if (getCourse() == null && that.getCourse() == null) {
            return Objects.equals(name, that.name) && Objects.equals(imageUrl, that.imageUrl);
        }
        if (getCourse() == null || that.getCourse() == null) {
            return false;
        }
        return Objects.equals(name, that.name) && Objects.equals(imageUrl, that.imageUrl)&& Objects.equals(getCourse().getId(), that.getCourse().getId());
    }

    @Override
    public int hashCode() {
        if (getCourse() == null) {
            return Objects.hash(super.hashCode(), name, imageUrl, null);
        }
        return Objects.hash(super.hashCode(), name, imageUrl, getCourse().getId());
    }
}
