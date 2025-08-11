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
@Entity(name = "UserEntity")
public class UserEntity {

    @Id
    @Column(name="id")
    private UUID id;

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<UserScoreEntity> leaderboardList = new ArrayList<>();

}

