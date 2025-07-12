package de.unistuttgart.iste.meitrex.gamification_service.service.entity;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.CompletedQuizzesGoalEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goals.GoalEntity;
import de.unistuttgart.iste.meitrex.generated.dto.Achievement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class AchievementEntityTest {
    private final GoalEntity goal = new CompletedQuizzesGoalEntity();
    private final GoalEntity goal2 = new CompletedQuizzesGoalEntity();
    private final UUID achievementId = UUID.randomUUID();
    private final CourseEntity course = new CourseEntity();
    private final CourseEntity course2 = new CourseEntity();
    private final CourseEntity courseSame = new CourseEntity();

    @BeforeEach
    public void setUp() {
        course.setId(UUID.randomUUID());
        course2.setId(UUID.randomUUID());
        courseSame.setId(course.getId());
    }

    private AchievementEntity createEntity(UUID id, String name, String imageUrl, GoalEntity goal, CourseEntity course) {
        AchievementEntity entity = new AchievementEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setImageUrl(imageUrl);
        entity.setGoal(goal);
        entity.setCourse(course);
        return entity;
    }

    @Test
    void testEquals_sameObject() {
        AchievementEntity entity = createEntity(UUID.randomUUID(), "A", "url", new CompletedQuizzesGoalEntity(), null);
        assertTrue(entity.equals(entity));
    }

    @Test
    void testEquals_nullObject() {
        AchievementEntity entity = createEntity(UUID.randomUUID(), "A", "url", new CompletedQuizzesGoalEntity(), null);
        assertFalse(entity.equals(null));
    }

    @Test
    void testEquals_differentClass() {
        AchievementEntity entity = createEntity(UUID.randomUUID(), "A", "url", new CompletedQuizzesGoalEntity(), null);
        assertFalse(entity.equals("not an AchievementEntity"));
    }

    @Test
    void testEquals_bothCoursesNull_equalFields() {
        AchievementEntity a1 = createEntity(achievementId, "A", "url", goal, null);
        AchievementEntity a2 = createEntity(achievementId, "A", "url", goal, null);
        assertTrue(a1.equals(a2));
    }

    @Test
    void testEquals_bothCoursesNull_differentFields() {
        AchievementEntity a1 = createEntity(UUID.randomUUID(), "A", "url", goal, null);
        AchievementEntity a2 = createEntity(UUID.randomUUID(), "B", "url2", goal2, null);
        assertFalse(a1.equals(a2));
    }

    @Test
    void testEquals_oneCourseNull_otherNot() {
        AchievementEntity a1 = createEntity(achievementId, "A", "url", goal, course);
        AchievementEntity a2 = createEntity(achievementId, "A", "url", goal, null);
        assertFalse(a1.equals(a2));
        assertFalse(a2.equals(a1));
    }

    @Test
    void testEquals_bothCoursesNotNull_sameId() {
        AchievementEntity a1 = createEntity(achievementId, "A", "url", goal, course);
        AchievementEntity a2 = createEntity(achievementId, "A", "url", goal, courseSame);
        assertTrue(a1.equals(a2));
    }

    @Test
    void testEquals_bothCoursesNotNull_differentId() {
        AchievementEntity a1 = createEntity(achievementId, "A", "url", goal, course);
        AchievementEntity a2 = createEntity(achievementId, "A", "url", goal, course2);
        assertFalse(a1.equals(a2));
    }
}
