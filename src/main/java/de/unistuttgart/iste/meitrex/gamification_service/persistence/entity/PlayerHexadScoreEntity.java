package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity;

import java.util.UUID;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "PlayerHexadScoreEntity")
public class PlayerHexadScoreEntity  implements IWithId<UUID> {

    @Id
    private UUID id;

    @OneToOne
    @JoinColumn(nullable = false)
    private UserEntity user;

    @NotNull
    @Column(nullable = false)
    @Min(0)
    @Max(100)
    private double philanthropist;

    @NotNull
    @Column(nullable = false)
    @Min(0)
    @Max(100)
    private double socialiser;

    @NotNull
    @Column(nullable = false)
    @Min(0)
    @Max(100)
    private double freeSpirit;

    @NotNull
    @Column(nullable = false)   
    @Min(0)
    @Max(100)
    private double achiever;

    @NotNull
    @Column(nullable = false)
    @Min(0)
    @Max(100)
    private double player;

    @NotNull
    @Column(nullable = false)
    @Min(0)
    @Max(100)
    private double disruptor;

}