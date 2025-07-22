package de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.recommendation;

import de.unistuttgart.iste.meitrex.common.persistence.MeitrexRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.recommendation.UserWidgetSettingsEntity;

import java.util.UUID;

public interface UserWidgetSettingsRepository extends MeitrexRepository<UserWidgetSettingsEntity, UUID> {
}
