package de.unistuttgart.iste.meitrex.gamification_service.persistence.repository;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.GoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.UserGoalProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserGoalProgressRepository extends JpaRepository<UserGoalProgressEntity, UUID> {
    List<UserGoalProgressEntity> findAllByUserAndGoal(UserEntity user, GoalEntity goal);
}
