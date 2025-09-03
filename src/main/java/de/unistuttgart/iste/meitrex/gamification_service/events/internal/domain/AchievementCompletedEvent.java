package de.unistuttgart.iste.meitrex.gamification_service.events.internal.domain;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.AchievementEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.Clock;
import java.util.Objects;
import java.util.UUID;


@Getter
public class AchievementCompletedEvent extends ApplicationEvent {

    private final AchievementEntity achievement;

    private final UserEntity achiever;

    public AchievementCompletedEvent(Object source, AchievementEntity achievement, UserEntity achiever) {
        super(source);
        this.achievement = Objects.requireNonNull(achievement);
        this.achiever = Objects.requireNonNull(achiever);
    }

    public AchievementCompletedEvent(Object source, Clock clock, AchievementEntity achievement, UserEntity achiever) {
        super(source, clock);
        this.achievement = Objects.requireNonNull(achievement);
        this.achiever = Objects.requireNonNull(achiever);
    }
}