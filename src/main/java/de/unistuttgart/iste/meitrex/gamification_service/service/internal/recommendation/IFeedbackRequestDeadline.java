package de.unistuttgart.iste.meitrex.gamification_service.service.internal.recommendation;

import de.unistuttgart.iste.meitrex.generated.dto.GamificationCategory;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface IFeedbackRequestDeadline {

    boolean isFeedbackRequestDue(@NotNull final UUID userId, @NotNull final GamificationCategory category);

}
