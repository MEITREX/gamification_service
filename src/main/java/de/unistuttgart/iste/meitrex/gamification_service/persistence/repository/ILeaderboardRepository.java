package de.unistuttgart.iste.meitrex.gamification_service.persistence.repository;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.LeaderboardEntity;
import de.unistuttgart.iste.meitrex.gamification_service.time.Period;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ILeaderboardRepository extends JpaRepository<LeaderboardEntity, UUID> {

    Optional<LeaderboardEntity> findByCourseAndPeriodOrderByStartDateDesc(CourseEntity courseEntity, Period period);

}
