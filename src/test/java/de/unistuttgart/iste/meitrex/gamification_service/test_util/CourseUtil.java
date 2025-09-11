package de.unistuttgart.iste.meitrex.gamification_service.test_util;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.AchievementRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements.ICourseAchievementMapper;
import de.unistuttgart.iste.meitrex.generated.dto.Chapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@TestComponent
public class CourseUtil {
    private final ICourseAchievementMapper achievementMapper;



    public CourseUtil(@Autowired ICourseAchievementMapper achievementMapper) {
        this.achievementMapper = achievementMapper;
    }

    public CourseEntity dummyCourseEntity(final UUID courseId, AchievementRepository achievementRepository) {
        CourseEntity courseEntity = new CourseEntity();
        courseEntity.setId(courseId);
        courseEntity.setTitle("Dummy Course");
        Chapter chapter = new Chapter();
        chapter.setId(UUID.randomUUID());
        chapter.setDescription("description");
        chapter.setTitle("title");
        courseEntity.setChapters(new ArrayList<>(List.of(chapter)));
        List<AchievementEntity> achievementEntities =
                new ArrayList<>(achievementMapper.map(courseEntity));
        courseEntity.setAchievements(achievementRepository.saveAll(achievementEntities));
        return courseEntity;
    }
}
