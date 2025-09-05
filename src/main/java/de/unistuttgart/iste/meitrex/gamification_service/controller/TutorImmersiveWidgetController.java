package de.unistuttgart.iste.meitrex.gamification_service.controller;

import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.gamification_service.service.TutorImmersiveWidgetService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

import static de.unistuttgart.iste.meitrex.common.user_handling.UserCourseAccessValidator.validateUserHasAccessToCourse;

@Slf4j
@Controller
@RequiredArgsConstructor
public class TutorImmersiveWidgetController {

    private final TutorImmersiveWidgetService tutorService;

    @QueryMapping
    public String tutorImmersiveWidgetSpeechContent(@Argument @NotNull final UUID courseId,
                                                    @ContextValue final LoggedInUser currentUser) {
        validateUserHasAccessToCourse(currentUser, LoggedInUser.UserRoleInCourse.STUDENT, courseId);

        return tutorService.getSpeechContent(currentUser, courseId);
    }
}
