package de.unistuttgart.iste.meitrex.gamification_service.test_util;

import de.unistuttgart.iste.meitrex.gamification_service.achievements.Achievements;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.AchievementRepository;
import de.unistuttgart.iste.meitrex.generated.dto.Chapter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CourseUtil {
    static Achievements achievements = new Achievements();
    public static CourseEntity dummyCourseEntity(final UUID courseId, AchievementRepository achievementRepository) {
        CourseEntity courseEntity = new CourseEntity();
        courseEntity.setId(courseId);
        Chapter chapter = new Chapter();
        chapter.setId(UUID.randomUUID());
        chapter.setDescription("description");
        chapter.setTitle("title");
        courseEntity.setChapters(new ArrayList<>(List.of(chapter)));
        List<AchievementEntity> achievementEntities = achievements.generateAchievements(courseEntity);
        achievementRepository.saveAll(achievementEntities);
        courseEntity.setAchievements(achievementEntities);
        return courseEntity;
    }
}
