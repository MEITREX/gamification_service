package de.unistuttgart.iste.meitrex.gamification_service.events.publication;

import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;

/**
 * Defines the contract for publishing internal application events. Implementations are responsible for persisting
 * events, ensuring that duplicate events are not processed, and publishing new events only after the surrounding
 * transaction has been successfully committed.
 *
 * @author Philipp Kunz
 */
public interface IEventPublicationService {


    /**
     * Saves the given event if it has not been processed before and publishes it after the current transaction
     * commits successfully as an instance of {@link de.unistuttgart.iste.meitrex.gamification_service.events.internal.InternalEvent}.
     *
     * @param persistentEvent the event to persist and publish (must not be {@code null})
     * @throws IllegalArgumentException if the event type is unsupported or the event is invalid
     */
    void saveCommitAndPublishIfNew(PersistentEvent persistentEvent);

}
