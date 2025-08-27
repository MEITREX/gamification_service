package de.unistuttgart.iste.meitrex.gamification_service.service;

import java.util.*;

import de.unistuttgart.iste.meitrex.generated.dto.*;

public interface IAchievementService {

    /**
     * Returns the achievements of the user with the given userId in the course with the
     *
     * @param userId   ID of the user to get achievements for.
     * @param courseId ID of the course to get achievements for.
     * @return A list of Achievement DTOs representing the user's achievements in the sp
     */
    List<Achievement> getAchievementsForUserInCourse(UUID userId, UUID courseId);

    /**
     * Gets all achievements of a user, including adaptive achievements, as DTOs.
     *
     * @param userId ID of the user to get achievements for.
     * @return A list of Achievement DTOs representing the user's achievements.
     */
    List<Achievement> getAchievementsForUser(UUID userId);

}

