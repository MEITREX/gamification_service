package de.unistuttgart.iste.meitrex.gamification_service.persistence.repository;

import de.unistuttgart.iste.meitrex.common.persistence.MeitrexRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.userGoalProgress.UserGoalProgressEntity;

import java.util.UUID;

public interface IUserGoalProgressRepository extends MeitrexRepository<UserGoalProgressEntity, UUID> {
}
