package de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements;

import java.util.List;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;

public interface ICourseAchievementMapper {
    List<AchievementEntity> map(CourseEntity course);
}
