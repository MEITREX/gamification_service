package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.time.Period;
import de.unistuttgart.iste.meitrex.generated.dto.Leaderboard;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ILeaderboardService {

    List<Leaderboard> find(UUID courseID, LocalDate date, Period period);

}
