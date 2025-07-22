package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "PlayerHexadScoreEntity")
public class PlayerHexadScoreEntity implements IWithId<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID userId;

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