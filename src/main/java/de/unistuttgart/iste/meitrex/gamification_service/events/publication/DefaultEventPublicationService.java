package de.unistuttgart.iste.meitrex.gamification_service.events.publication;

import de.unistuttgart.iste.meitrex.gamification_service.events.internal.InternalContentProgressedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.InternalEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.InternalForumActivityEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.InternalUserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

    private final IPersistentContentProgressedRepository contentProgressedRepository;

    private final IPersistentForumActivityRepository forumActivityRepository;

    private final IPersistentSkillEntityChangedEventRepository skillEntityChangedEventRepository;

    private final IPersistentUserSkillLevelChangedEventRepository userSkillLevelChangedEventRepository;

    private final Map<Class<? extends PersistentEvent>, Function<PersistentEvent, InternalEvent>> handlerMap = new HashMap<>();


    public DefaultEventPublicationService(
            @Autowired TransactionalApplicationEventPublisher applicationEventPublisher,
            @Autowired @Qualifier("default") IPersistentEventRepository persistentEventRepository,
            @Autowired IPersistentUserProgressUpdatedRepository userProgressUpdatedRepository,
            @Autowired IPersistentContentProgressedRepository contentProgressedRepository,
            @Autowired IPersistentForumActivityRepository forumActivityRepository,
            @Autowired IPersistentSkillEntityChangedEventRepository skillEntityChangedEventRepository,
            @Autowired IPersistentUserSkillLevelChangedEventRepository userSkillLevelChangedEventRepository
    ) {
        this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher);
        this.persistentEventRepository = Objects.requireNonNull(persistentEventRepository);
        this.userProgressUpdatedRepository = Objects.requireNonNull(userProgressUpdatedRepository);
        this.contentProgressedRepository = Objects.requireNonNull(contentProgressedRepository);
        this.forumActivityRepository = Objects.requireNonNull(forumActivityRepository);
        this.skillEntityChangedEventRepository = Objects.requireNonNull(skillEntityChangedEventRepository);
        this.userSkillLevelChangedEventRepository = Objects.requireNonNull(userSkillLevelChangedEventRepository);
        this.handlerMap.put(PersistentUserProgressUpdatedEvent.class, this::saveUserProgressUpdatedEvent);
        this.handlerMap.put(PersistentContentProgressedEvent.class, this::saveContentProgressedEvent);
        this.handlerMap.put(PersistentForumActivityEvent.class, this::saveForumActivityEvent);
        this.handlerMap.put(PersistentSkillEntityChangedEvent.class, this::saveSkillEntityChangedEvent);
        this.handlerMap.put(PersistentUserSkillLevelChangedEvent.class, this::saveUserSkillLevelChangedEvent);
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
        final Long seqNo = persistentEvent.getMsgSequenceNo();

        if(Objects.isNull(seqNo)) {
            throw new IllegalArgumentException(ERR_MSG_NO_SEQ_NO);
        }

        return this.persistentEventRepository.
                findByMsgSequenceNo(seqNo)
                .isEmpty();

    }

    private InternalEvent saveUserProgressUpdatedEvent(PersistentEvent persistentEvent) {

        PersistentUserProgressUpdatedEvent persistentUserProgressEvent = (PersistentUserProgressUpdatedEvent) persistentEvent;

        final UUID uuid = this.userProgressUpdatedRepository.save(persistentUserProgressEvent)
                .getUuid();

        return new InternalUserProgressUpdatedEvent(DefaultEventPublicationService.this, uuid);
    }

    private InternalEvent saveContentProgressedEvent(PersistentEvent persistentEvent) {

        PersistentContentProgressedEvent persistentContentProgressedEvent = (PersistentContentProgressedEvent) persistentEvent;

        final UUID uuid = this.contentProgressedRepository.save(persistentContentProgressedEvent)
                .getUuid();

        return new InternalContentProgressedEvent(DefaultEventPublicationService.this, uuid);
    }

    private InternalEvent saveForumActivityEvent(PersistentEvent persistentEvent) {
        PersistentForumActivityEvent persistentForumActivityEvent = (PersistentForumActivityEvent) persistentEvent;

        final UUID uuid = this.forumActivityRepository.save(persistentForumActivityEvent)
                .getUuid();

        return new InternalForumActivityEvent(DefaultEventPublicationService.this, uuid);
    }

    private InternalEvent saveSkillEntityChangedEvent(PersistentEvent persistentEvent) {
        PersistentForumActivityEvent persistentForumActivityEvent = (PersistentForumActivityEvent) persistentEvent;

        final UUID uuid = this.forumActivityRepository.save(persistentForumActivityEvent)
                .getUuid();

        return new InternalForumActivityEvent(DefaultEventPublicationService.this, uuid);
    }

    private InternalEvent saveUserSkillLevelChangedEvent(PersistentEvent persistentEvent) {
        PersistentForumActivityEvent persistentForumActivityEvent = (PersistentForumActivityEvent) persistentEvent;

        final UUID uuid = this.forumActivityRepository.save(persistentForumActivityEvent)
                .getUuid();

        return new InternalForumActivityEvent(DefaultEventPublicationService.this, uuid);
    }

}
