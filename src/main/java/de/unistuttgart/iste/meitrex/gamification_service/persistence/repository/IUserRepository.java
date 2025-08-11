package de.unistuttgart.iste.meitrex.gamification_service.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;

import java.util.UUID;

public interface IUserRepository extends JpaRepository<UserEntity, UUID> { }

