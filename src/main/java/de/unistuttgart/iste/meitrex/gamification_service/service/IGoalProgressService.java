package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;

import java.util.UUID;

public interface IGoalProgressService {

    UUID loginUser(UUID userId, UUID courseId);

    void itemReceivedProgress(UserEntity user);

    void equipItemProgress(UserEntity user);

    void lotteryRunProgress(UserEntity user);
}
