package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity;

import java.util.*;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.generated.dto.Chapter;
import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity(name = "CourseEntity")
public class CourseEntity implements IWithId<UUID> {

    @Id
    @Column(name="id")
    private UUID id;

    @Column(name="title")
    private String title;

    @ElementCollection
    private List<Chapter> chapters;

    @OneToMany(cascade = CascadeType.ALL)
    private List<AchievementEntity> achievements;

    @Builder.Default
    @OneToMany(mappedBy = "course")
    private  List<LeaderboardEntity> leaderboardEntityList = new ArrayList<>();

}
