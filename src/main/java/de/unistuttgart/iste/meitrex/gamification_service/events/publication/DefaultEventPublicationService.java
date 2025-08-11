package de.unistuttgart.iste.meitrex.gamification_service.events.publication;

import de.unistuttgart.iste.meitrex.gamification_service.events.internal.InternalEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.InternalUserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentUserProgressUpdatedRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Component
class DefaultEventPublicationService implements IEventPublicationService {

    private static final String ERR_MSG_UNSUPPORTED_EVENT_TYPE = "The passed persistent event is not supported.";

    private static final String ERR_MSG_NO_SEQ_NO = "A persist event must feature a valid sequence no.";


    private final TransactionalApplicationEventPublisher applicationEventPublisher;

    private final IPersistentEventRepository persistentEventRepository;

    private final IPersistentUserProgressUpdatedRepository userProgressUpdatedRepository;

    private final Map<Class<? extends PersistentEvent>, Function<PersistentEvent, InternalEvent>> handlerMap = new HashMap<>();

    public DefaultEventPublicationService(
            @Autowired TransactionalApplicationEventPublisher applicationEventPublisher,
            @Autowired IPersistentEventRepository persistentEventRepository,
            @Autowired IPersistentUserProgressUpdatedRepository userProgressUpdatedRepository
    ) {
        this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher);
        this.persistentEventRepository = Objects.requireNonNull(persistentEventRepository);
        this.userProgressUpdatedRepository = Objects.requireNonNull(userProgressUpdatedRepository);
        this.handlerMap.put(PersistentUserProgressUpdatedEvent.class, this::saveUserProgressUpdatedEvent);
    }

    @Override
    public void saveCommitAndPublishIfNew(PersistentEvent persistentEvent) {

        if(!this.handlerMap.containsKey(persistentEvent.getClass())) {
            throw new IllegalArgumentException(ERR_MSG_UNSUPPORTED_EVENT_TYPE);
        }

        //if(true || isNew(persistentEvent)) {
            final InternalEvent internalEvent = this.handlerMap.get(persistentEvent.getClass())
                    .apply(persistentEvent);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    applicationEventPublisher.publishEvent(internalEvent);
                }
            });
        /*}
        else {
            log.info("Ignored message {} since the passed sequence no  {} has already been passed.", persistentEvent, persistentEvent.getSequenceNo());
        }*/
    }

    private boolean isNew(PersistentEvent persistentEvent) {
        final Long seqNo = persistentEvent.getSequenceNo();

        if(Objects.isNull(seqNo)) {
            throw new IllegalArgumentException(ERR_MSG_NO_SEQ_NO);
        }

        return this.persistentEventRepository.
                findBySequenceNo(seqNo)
                .isEmpty();

    }


    private InternalEvent saveUserProgressUpdatedEvent(PersistentEvent persistentEvent) {

        PersistentUserProgressUpdatedEvent persistentUserProgressEvent = (PersistentUserProgressUpdatedEvent) persistentEvent;

        final UUID uuid = this.userProgressUpdatedRepository.save(persistentUserProgressEvent)
                .getUuid();

        return new InternalUserProgressUpdatedEvent(DefaultEventPublicationService.this, uuid);
    }

}
