package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import lombok.*;
import jakarta.persistence.*;

import java.util.UUID;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity(name = "LeaderboardRankRewardEntity")
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"rank", "fk_leaderboard"})
        }
)
public class LeaderboardRankRewardEntity implements IWithId<UUID> {

    @NonNull
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private UUID id;

    @NonNull
    @Column(name="rank", nullable = false)
    private Integer rank;

    // Foreign key relationships

    @NonNull
    @ManyToOne
    @JoinColumn(name="fk_leaderboard", nullable = false)
    private LeaderboardEntity leaderboard;

}
