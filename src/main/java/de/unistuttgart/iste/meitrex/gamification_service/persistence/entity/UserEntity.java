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
@Entity(name = "UserEntity")
public class UserEntity {

    @Id
    @Column(name="id")
    private UUID id;

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<UserScoreEntity> leaderboardList = new ArrayList<>();

}

