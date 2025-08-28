package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity;

import java.util.*;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.UserInventoryEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.skilllevels.SkillEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.skilllevels.SkillLevelsEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import de.unistuttgart.iste.meitrex.common.persistence.IWithId;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity(name = "UserEntity")
public class UserEntity implements IWithId<UUID> {

    @Id
    @Column(name="id")
    private UUID id;

    @Column(name="xp_value", nullable = false)
    private Integer xpValue;

    @Transient
    private Double requiredXP;

    @Transient
    private Double exceedingXP;

    @Transient
    private Integer level;

    @OneToMany(cascade = CascadeType.ALL)
    private List<UserCourseDataEntity> courseData;

    @OneToOne(cascade = CascadeType.ALL)
    private UserInventoryEntity inventory;

    @OneToMany(cascade = CascadeType.ALL)
    @NotNull
    private List<SkillLevelsEntity> skillLevels;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private PlayerHexadScoreEntity playerHexadScore;

    @Builder.Default
    @OneToMany(mappedBy = "user")
    private List<UserScoreEntity> leaderboardList = new ArrayList<>();

    public Optional<UserCourseDataEntity> getCourseData(UUID courseId) {
        return courseData.stream()
                .filter(data -> data.getCourseId().equals(courseId))
                .findFirst();
    }

    public Optional<SkillLevelsEntity> getSkillLevelsForSkill(UUID skillId) {
        return skillLevels.stream()
                .filter(skillLevel -> skillLevel.getSkill().getId().equals(skillId))
                .findFirst();
    }
}

