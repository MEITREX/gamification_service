package de.unistuttgart.iste.meitrex.gamification_service.test_util;

import de.unistuttgart.iste.meitrex.gamification_service.achievements.Achievements;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.AchievementRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements.ICourseAchievementMapper;
import de.unistuttgart.iste.meitrex.generated.dto.Chapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class CourseUtil {
    private final ICourseAchievementMapper achievementMapper;

    public CourseUtil(@Autowired ICourseAchievementMapper achievementMapper) {
        this.achievementMapper = achievementMapper;
    }

    public static CourseEntity dummyCourseEntity(final UUID courseId, AchievementRepository achievementRepository) {
        CourseEntity courseEntity = new CourseEntity();
        courseEntity.setId(courseId);
        Chapter chapter = new Chapter();
        chapter.setId(UUID.randomUUID());
        chapter.setDescription("description");
        chapter.setTitle("title");
        courseEntity.setChapters(new ArrayList<>(List.of(chapter)));
        List<AchievementEntity> achievementEntities = achievementMapper.map(courseEntity);
        achievementRepository.saveAll(achievementEntities);
        courseEntity.setAchievements(achievementEntities);
        return courseEntity;
    }
}
