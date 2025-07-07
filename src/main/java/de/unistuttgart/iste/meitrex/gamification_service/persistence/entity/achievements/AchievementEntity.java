package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity(name = "Achievement")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AchievementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID id;

    @Column
    String name;

    @Column
    String imageUrl;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    GoalEntity goal;

    @ManyToOne(fetch = FetchType.LAZY)
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
}
