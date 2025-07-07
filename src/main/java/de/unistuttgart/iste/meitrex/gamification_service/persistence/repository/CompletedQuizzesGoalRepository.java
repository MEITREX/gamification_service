package de.unistuttgart.iste.meitrex.gamification_service.persistence.repository;

import de.unistuttgart.iste.meitrex.common.persistence.MeitrexRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.CompletedQuizzesGoalEntity;

import java.util.List;
import java.util.UUID;

public interface CompletedQuizzesGoalRepository extends MeitrexRepository<CompletedQuizzesGoalEntity, UUID> {
    List<CompletedQuizzesGoalEntity> findCompletedQuizzesGoalEntitiesByAchievement(AchievementEntity achievement);
}
