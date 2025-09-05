package de.unistuttgart.iste.meitrex.gamification_service.service.internal;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.skilllevels.SkillEntity;

import java.util.UUID;

public interface ISkillCreator {
    SkillEntity fetchOrCreate(final UUID skillId);
}
