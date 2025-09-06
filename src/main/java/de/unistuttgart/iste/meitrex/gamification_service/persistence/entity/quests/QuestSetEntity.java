package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestSetEntity implements IWithId<UUID> {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The name of the quest set.
     */
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private CourseEntity course;

    /**
     * For which day is this quest set intended?
     * For daily quests, this represents the day the quests are intended for. If the day passes, new quests will be
     * generated.
     * For returning user quests, there are multiple quest sets for concurrent days. As long as the returning user
     * quests are active, the user can complete quests whose "forDay" has passed (or is today).
     */
    private LocalDate forDay;

    private float rewardMultiplier;

    @OneToMany(cascade = CascadeType.ALL)
    private List<QuestEntity> quests;
}
