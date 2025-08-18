package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.UserInventoryEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.skilllevels.SkillEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.skilllevels.SkillLevelsEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserEntity implements IWithId<UUID> {
    @Id
    UUID id;

    @OneToMany(cascade = CascadeType.ALL)
    List<UserCourseDataEntity> courseData;

    @OneToOne(cascade = CascadeType.ALL)
    UserInventoryEntity inventory;

    @OneToMany(cascade = CascadeType.ALL)
    @NotNull
    List<SkillLevelsEntity> skillLevels;

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
