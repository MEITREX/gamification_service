package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity;

import de.unistuttgart.iste.meitrex.gamification_service.time.Period;
import lombok.*;
import jakarta.persistence.*;


import java.time.LocalDate;
import java.util.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity(name = "LeaderboardEntity")
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"start_date", "period", "fk_course"})
        }
)
public class LeaderboardEntity {

    // Primary key

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @NonNull
    @Column(name="title", nullable = false)
    private String title;

    // The overall interval is inferred using a start date and the respective period.

    @NonNull
    @Column(name="start_date", nullable = false)
    private LocalDate startDate;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name="period", nullable = false)
    private Period period;

    // Foreign key relationships

    @NonNull
    @ManyToOne
    @JoinColumn(name="fk_course", nullable = false)
    private CourseEntity course;

    @Builder.Default
    @OneToMany(mappedBy = "leaderboard")
    private List<UserScoreEntity> scoreEntityList = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "leaderboard")
    private List<LeaderboardRankRewardEntity> leaderboardRankRewardList = new ArrayList<>();



}