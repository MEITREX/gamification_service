package de.unistuttgart.iste.meitrex.gamification_service.events.publication;

import de.unistuttgart.iste.meitrex.gamification_service.events.internal.InternalEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.InternalUserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentUserProgressUpdatedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@Component
class DefaultEventPublicationService implements IEventPublicationService {

    private static final String ERR_MSG_UNSUPPORTED_EVENT_TYPE = "The passed persistent event is not supported.";

    private final TransactionalApplicationEventPublisher applicationEventPublisher;

    private final IPersistentUserProgressUpdatedRepository userProgressUpdatedRepository;

    private final Map<Class<? extends PersistentEvent>, Function<PersistentEvent, InternalEvent>> handlerMap = new HashMap<>();

    public DefaultEventPublicationService(
            @Autowired TransactionalApplicationEventPublisher applicationEventPublisher,
            @Autowired IPersistentUserProgressUpdatedRepository userProgressUpdatedRepository
    ) {
        this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher);
        this.userProgressUpdatedRepository = Objects.requireNonNull(userProgressUpdatedRepository);

        this.handlerMap.put(PersistentUserProgressUpdatedEvent.class, this::saveUserProgressUpdatedEvent);
    }

    @Override
    public void saveCommitAndPublish(PersistentEvent persistentEvent) {

        if(!this.handlerMap.containsKey(persistentEvent.getClass())) {
            throw new IllegalArgumentException(ERR_MSG_UNSUPPORTED_EVENT_TYPE);
        }

        final InternalEvent internalEvent = this.handlerMap.get(persistentEvent.getClass())
                .apply(persistentEvent);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                applicationEventPublisher.publishEvent(internalEvent);
            }
        });
    }


    private InternalEvent saveUserProgressUpdatedEvent(PersistentEvent persistentEvent) {

        PersistentUserProgressUpdatedEvent persistentUserProgressEvent = (PersistentUserProgressUpdatedEvent) persistentEvent;

        final UUID uuid = this.userProgressUpdatedRepository.save(persistentUserProgressEvent)
                .getUuid();

        return new InternalUserProgressUpdatedEvent(DefaultEventPublicationService.this, uuid);
    }

}
