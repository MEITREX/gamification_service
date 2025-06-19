package de.unistuttgart.iste.meitrex.gamification_service.persistence.repository;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CompleteSpecificChapterGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CompletedQuizzesGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CompletedQuizzesGoalRepository extends JpaRepository<CompletedQuizzesGoalEntity, UUID> {
    List<CompletedQuizzesGoalEntity> findCompletedQuizzesGoalEntitiesByAchievement(AchievementEntity achievement);
}
