package de.unistuttgart.iste.meitrex.gamification_service.persistence.repository;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ICourseRepository extends JpaRepository<CourseEntity, UUID> { }
