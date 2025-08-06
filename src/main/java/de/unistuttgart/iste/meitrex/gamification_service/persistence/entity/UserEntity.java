package de.unistuttgart.iste.meitrex.gamification_service.persistence.entity;

import de.unistuttgart.iste.meitrex.common.persistence.IWithId;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.items.UserInventoryEntity;
import jakarta.persistence.*;
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

    public Optional<UserCourseDataEntity> getCourseData(UUID courseId) {
        return courseData.stream()
                .filter(data -> data.getCourseId().equals(courseId))
                .findFirst();
    }
}
