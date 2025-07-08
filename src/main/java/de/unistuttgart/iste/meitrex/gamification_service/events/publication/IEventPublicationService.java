package de.unistuttgart.iste.meitrex.gamification_service.events.publication;

import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;

public interface IEventPublicationService {

    void saveCommitAndPublish(PersistentEvent persistentEvent);

}
