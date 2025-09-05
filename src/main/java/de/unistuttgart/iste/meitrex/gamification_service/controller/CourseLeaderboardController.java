package de.unistuttgart.iste.meitrex.gamification_service.controller;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.LeaderboardEntity;
import de.unistuttgart.iste.meitrex.gamification_service.service.ILeaderboardService;
import de.unistuttgart.iste.meitrex.gamification_service.time.Period;
import de.unistuttgart.iste.meitrex.generated.dto.Leaderboard;
import lombok.*;
import lombok.extern.slf4j.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.*;


import java.util.*;
import java.time.*;


@Slf4j
@RequiredArgsConstructor
@Controller
public class CourseLeaderboardController {

    private final ILeaderboardService leaderboardService;

    @QueryMapping
    public List<Leaderboard> getWeeklyCourseLeaderboards(@Argument UUID courseID, @Argument String date) {
        return leaderboardService.find(courseID, LocalDate.parse(date), Period.WEEKLY);
    }

    @QueryMapping
    public List<Leaderboard> getMonthlyCourseLeaderboards(@Argument UUID courseID, @Argument String date) {
        return leaderboardService.find(courseID, LocalDate.parse(date), Period.MONTHLY);
    }

    @QueryMapping
    public List<Leaderboard> getAllTimeCourseLeaderboards(@Argument UUID courseID, @Argument String date) {
        return leaderboardService.find(courseID, LocalDate.parse(date), Period.ALL_TIME);
    }

}
