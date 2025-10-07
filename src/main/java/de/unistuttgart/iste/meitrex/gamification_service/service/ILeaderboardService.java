package de.unistuttgart.iste.meitrex.gamification_service.service;

import java.time.*;
import java.util.*;

import de.unistuttgart.iste.meitrex.generated.dto.*;

import de.unistuttgart.iste.meitrex.gamification_service.time.Period;

/**
 * A service layer interface for fetching information reflecting a users performance as visualized by a leaderboard. The
 * results are meant for external exposure, .g. via a GraphQL API, and should not be consumed by business logic.
 *
 * @author Philipp Kunz
 */
public interface ILeaderboardService {

    /**
     * Retrieves leaderboard data representing the relative performance of users participating in the course identified by
     * {@code courseID}, within the time range defined by the given {@code date} and {@code period}.
     * If no matching leaderboard data is found, an empty list is returned.
     *
     * @param courseID the uuid of the course for which leaderboard data should be retrieved.
     * @param date the start of the period of interest.
     * @param period the period that, together with {@code date}, determines the duration of the leaderboard
     *               (e.g. daily, weekly, monthly).
     * @return a list of leaderboards.
     */
    List<Leaderboard> find(UUID courseID, LocalDate date, Period period);

}
