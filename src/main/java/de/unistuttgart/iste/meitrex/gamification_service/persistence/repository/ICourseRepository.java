package de.unistuttgart.iste.meitrex.gamification_service.persistence.repository;

import java.util.*;

import org.springframework.data.jpa.repository.*;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.*;

public interface ICourseRepository extends JpaRepository<CourseEntity, UUID> { }
