package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity;

import java.util.*;

import jakarta.persistence.*;
import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity(name = "CourseEntity")
public class CourseEntity {

    @Id
    @Column(name="id")
    private UUID id;

    @Column(name="title")
    private String title;

    @Builder.Default
    @OneToMany(mappedBy = "course")
    private  List<LeaderboardEntity> leaderboardEntityList = new ArrayList<>();

}
