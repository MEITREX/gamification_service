package de.unistuttgart.iste.meitrex.gamification_service.persistence.repository;

import java.util.*;

import de.unistuttgart.iste.meitrex.gamification_service.time.Period;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.*;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.*;

public interface IUserScoreRepository extends JpaRepository<UserScoreEntity, UUID> {

    /**
     * Returns the most recent {@link UserScoreEntity} for the given user, course, and period type,
     * based on the start date of the associated leaderboard.
     *
     * @param user - a user
     * @param course - a course
     * @param period - a period.
     * @return - an optional of the most recent {@link UserScoreEntity} or an empty optional if no such instance exists.
     */
    @Query(
            "SELECT o FROM UserScoreEntity o " +
                    "JOIN o.leaderboard l " +
                    "WHERE o.user = :user AND l.course = :course AND l.period = :period " +
                    "ORDER BY l.startDate DESC"
    )
    Optional<UserScoreEntity> findMostRecentUserScore(
            @Param("user") UserEntity user,
            @Param("course") CourseEntity course,
            @Param("period") Period period
    );

}
