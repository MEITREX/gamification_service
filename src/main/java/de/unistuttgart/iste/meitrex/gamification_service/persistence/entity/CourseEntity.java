package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


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
