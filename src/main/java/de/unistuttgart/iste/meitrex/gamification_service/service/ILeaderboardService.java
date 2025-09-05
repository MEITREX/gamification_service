package de.unistuttgart.iste.meitrex.gamification_service.service;

import java.time.*;
import java.util.*;

import de.unistuttgart.iste.meitrex.generated.dto.*;

import de.unistuttgart.iste.meitrex.gamification_service.time.Period;

public interface ILeaderboardService {

    List<Leaderboard> find(UUID courseID, LocalDate date, Period period);

}
