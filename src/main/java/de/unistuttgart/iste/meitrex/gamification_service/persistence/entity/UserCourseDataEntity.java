package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.quests.QuestSetEntity;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCourseDataEntity implements IWithId<UUID> {
    @Id
    @GeneratedValue
    private UUID id;

    private UUID courseId;

    @OneToMany(cascade = CascadeType.ALL)
    @NotNull
    private List<UserGoalProgressEntity> goalProgressEntities;

    @OneToOne(cascade = CascadeType.ALL)
    @Nullable
    private QuestSetEntity dailyQuestSet;

    @OneToMany(cascade = CascadeType.ALL)
    @NotNull
    private List<QuestSetEntity> returningUserQuestSets;
}
