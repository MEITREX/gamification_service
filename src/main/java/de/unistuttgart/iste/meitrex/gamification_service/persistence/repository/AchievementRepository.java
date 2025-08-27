package de.unistuttgart.iste.meitrex.gamification_service.persistence.repository;

import de.unistuttgart.iste.meitrex.common.persistence.MeitrexRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AchievementRepository extends MeitrexRepository<AchievementEntity, UUID> {
    List<AchievementEntity> getAchievementEntitiesByCourse(CourseEntity course);
}
