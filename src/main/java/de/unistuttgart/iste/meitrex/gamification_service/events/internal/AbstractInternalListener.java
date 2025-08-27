package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.event.EventListener;

import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class AbstractInternalListener<U extends PersistentEvent, V extends InternalEvent> {


    private static final String ERR_MSG_MISSING_PERSISTENT_EVENT = "There is no persistent event matching the passed internal event featuring the uuid %s.";


    private static final int MAX_ATTEMPT_COUNT = 3;


    private static void assureIsValid(InternalEvent event) {
        Objects.requireNonNull(event.getId());
    }


    // Dependencies

    private final IPersistentEventStatusRepository eventStatusRepository;

    private final IPersistentEventRepository<U> persistentEventRepository;

    private final ITimeService timeService;

    public AbstractInternalListener(IPersistentEventRepository<U> persistentEventRepository, IPersistentEventStatusRepository eventStatusRepository, ITimeService timeService) {
        this.persistentEventRepository = Objects.requireNonNull(persistentEventRepository);
        this.eventStatusRepository = Objects.requireNonNull(eventStatusRepository);
        this.timeService = Objects.requireNonNull(timeService);
    }


    public final UUID getListenerUUID() {
        return UUID.nameUUIDFromBytes(getName().getBytes(StandardCharsets.UTF_8));
    }

    public void process(V internalEvent) {
        assureIsValid(internalEvent);
        process(fetchPersistentEvent(internalEvent));
    }

    private U fetchPersistentEvent(V internalEvent) {
        Optional<U> persistentEventOptional = this.persistentEventRepository.findById(internalEvent.getId());
        if(persistentEventOptional.isEmpty()) {
            throw new IllegalStateException(String.format(ERR_MSG_MISSING_PERSISTENT_EVENT, internalEvent.getId()));
        }
        return persistentEventOptional.get();
    }



    private void process(U persistentEvent) {
        PersistentEvent.PersistentEventStatus status = createOrUpdatePersistentEventStatus(persistentEvent);
        try {
            doProcess(persistentEvent);
            onProcessingCompleted(status);
        }
        catch (NonTransientEventListenerException e1) {
            onNonTransientInternalEventListenerException(status, e1);
        }
        catch(RuntimeException e0) {
            onTransientEventListenerException(status, e0);
        }
    }


    // Status handling

    private PersistentEvent.PersistentEventStatus createOrUpdatePersistentEventStatus(U event) {
        return getEventStatus(event)
                .map(this::updatePersistentEventStatus)
                .orElseGet(() -> createPersistentEventStatus(event));
    }

    private PersistentEvent.PersistentEventStatus  createPersistentEventStatus(U event) {
        final PersistentEvent.PersistentEventStatus status = new PersistentEvent.PersistentEventStatus();
        status.setStatus(PersistentEvent.PersistentEventStatus.Status.RECEIVED);
        status.setCurAttempt(1);
        status.setMaxAttemptCount(MAX_ATTEMPT_COUNT);
        status.setLastProcessingAttemptTimestamp(timeService.curTime());
        status.setInternalEventListenerId(getListenerUUID());
        status.setPersistentEvent(event);
        event.getPersistentEventStatusList().add(status);
        return this.eventStatusRepository.save(status);
    }

    private PersistentEvent.PersistentEventStatus updatePersistentEventStatus(PersistentEvent.PersistentEventStatus status) {
        int lastAttempt = status.getCurAttempt();
        int curAttempt = ++lastAttempt;
        status.setCurAttempt(curAttempt);
        return status;
    }

    //

    private void onProcessingCompleted(PersistentEvent.PersistentEventStatus status ) {
        status.setStatus(PersistentEvent.PersistentEventStatus.Status.PROCESSED);
    }

    private void onTransientEventListenerException(PersistentEvent.PersistentEventStatus status , RuntimeException e0) {
        if(status.getCurAttempt() < status.getMaxAttemptCount()) {
            status.setStatus(PersistentEvent.PersistentEventStatus.Status.FAILED_RETRY);
        }
        else {
            status.setStatus(PersistentEvent.PersistentEventStatus.Status.FAILED);
        }
    }

    private void onNonTransientInternalEventListenerException(PersistentEvent.PersistentEventStatus status , NonTransientEventListenerException e1) {
        status.setStatus(PersistentEvent.PersistentEventStatus.Status.FAILED);
    }


    // Utility method to fetch a persistent event's current status.

    private Optional<PersistentEvent.PersistentEventStatus> getEventStatus(U event) {
        final UUID uuid = this.getListenerUUID();
        return event.getPersistentEventStatusList()
                .stream()
                .filter(persistentEventStatus -> uuid.equals(persistentEventStatus.getInternalEventListenerId()))
                .findFirst();
    }



    protected abstract String getName();

    protected abstract void doProcess(U persistentEvent)
            throws TransientEventListenerException, NonTransientEventListenerException;

}
