package de.unistuttgart.iste.meitrex.gamification_service.persistence.repository;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.LeaderboardEntity;
import de.unistuttgart.iste.meitrex.gamification_service.time.Period;
import de.unistuttgart.iste.meitrex.generated.dto.Leaderboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ILeaderboardRepository extends JpaRepository<LeaderboardEntity, UUID> {

    Optional<LeaderboardEntity> findByCourseAndPeriodOrderByStartDateDesc(CourseEntity courseEntity, Period period);

    @Query("SELECT l FROM LeaderboardEntity l WHERE l.id = :id AND l.startDate >= :startDate AND l.period = :period")
    List<LeaderboardEntity> findByCourseIdAndDateAfterAndPeriod(
            @Param("id") UUID courseId,
            @Param("startDate") LocalDate date,
            @Param("period") Period period
    );
}
