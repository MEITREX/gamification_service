package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.apache.catalina.User;

import java.util.Date;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity(name = "UserScoreEntity")
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"fk_user", "fk_leader_board"})
        }
)
public class UserScoreEntity implements IWithId<UUID> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private UUID id;


    @ManyToOne
    @JoinColumn(name="fk_user", nullable = false)
    private UserEntity user;

    @Min(0)
    @NonNull
    @Column(name="score", nullable = false)
    @Builder.Default
    private Double score = 0.0;

    // Foreign key relationships

    @ManyToOne
    @JoinColumn(name="fk_leader_board", nullable = false)
    private LeaderboardEntity leaderboard;

}