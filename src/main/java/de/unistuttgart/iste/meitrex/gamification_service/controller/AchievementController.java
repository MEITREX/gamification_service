package de.unistuttgart.iste.meitrex.gamification_service.controller;

import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.gamification_service.service.AchievementService;
import de.unistuttgart.iste.meitrex.gamification_service.service.GoalProgressService;
import de.unistuttgart.iste.meitrex.generated.dto.Achievement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

import static de.unistuttgart.iste.meitrex.common.user_handling.UserCourseAccessValidator.validateUserHasAccessToCourse;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AchievementController {
    private final AchievementService achievementService;
    private final GoalProgressService goalProgressService;

    @QueryMapping
    public List<Achievement> achievementsByCourseId(@Argument UUID courseId,
                                                    @ContextValue final LoggedInUser currentUser) {
        validateUserHasAccessToCourse(currentUser, LoggedInUser.UserRoleInCourse.STUDENT, courseId);
        return achievementService.getAchievementsForUserInCourse(currentUser.getId(), courseId);
    }

    @QueryMapping
    public List<Achievement> achievementsByUserId(@Argument UUID userId,
            @ContextValue final LoggedInUser currentUser) {
        return achievementService.getAchievementsForUser(userId);
    }

    @MutationMapping
    public UUID loginUser(@Argument final UUID courseId,
            @ContextValue final LoggedInUser currentUser) {
        validateUserHasAccessToCourse(currentUser, LoggedInUser.UserRoleInCourse.STUDENT, courseId);
        return goalProgressService.loginUser(currentUser.getId(), courseId);
    }
}
