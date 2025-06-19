package de.unistuttgart.iste.meitrex.gamification_service.controller;

import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.gamification_service.service.AchievementService;
import de.unistuttgart.iste.meitrex.generated.dto.Achievement;
import de.unistuttgart.iste.meitrex.generated.dto.UserGoalProgress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.Arguments;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;
import static de.unistuttgart.iste.meitrex.common.user_handling.UserCourseAccessValidator.validateUserHasAccessToCourse;
import static de.unistuttgart.iste.meitrex.common.user_handling.UserCourseAccessValidator.validateUserHasAccessToCourses;

@Slf4j
@Controller
@RequiredArgsConstructor
public class AchievementController {
    private final AchievementService achievementService;

    @QueryMapping
    public List<UserGoalProgress> achievements(@Argument UUID courseId,
                                               @ContextValue final LoggedInUser currentUser) {
        validateUserHasAccessToCourse(currentUser, LoggedInUser.UserRoleInCourse.STUDENT, courseId);
        return achievementService.getAchievementsForUser(currentUser.getId(), courseId);
    }
}
