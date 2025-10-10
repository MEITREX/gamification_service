package de.unistuttgart.iste.meitrex.gamification_service.events.internal;

import de.unistuttgart.iste.meitrex.gamification_service.aspects.logging.Loggable;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.event.EventListener;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Abstract base class for internal event listeners in the gamification service. This listener implements generic
 * functionality for processing {@link PersistentEvent} and their corresponding {@link InternalEvent}s It handles:
 * (1) fetching the associated persistent event from the repository, (2) tracking and updating processing status
 * for retries and failures, (3) delegating the actual business logic to subclasses via {@link #doProcess(PersistentEvent)},
 * (4) retry logic with configurable max attempt count. Any concrete listener is expected to be executed within a
 * transactional context (e.g., managed by Spring), otherwise, event status updates will not be persisted.
 *
 * To implement a custom listener for a specific type of event, create a subclass overriding the following methods:
 * (1) {@link #getName()} - must return a unique and stable name.
 * (2) {@link #doProcess(U persistentEvent))} - contains the actual processing logic. In case of an unrecoverable error,
 * an implementation should throw a {@link NonTransientEventListenerException}. Otherwise, {@link TransientEventListenerException}
 * should be thrown. In the latter case, the base class logic takes care of updating the event status and retrying.
 *
 * @param <U> the type of the persistent event associated with the internal event.
 * @param <V> the type of the internal event to be processed.
 * @author Philiipp Kunz
 */
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

    @Loggable(
            inLogLevel = Loggable.LogLevel.INFO,
            exitLogLevel = Loggable.LogLevel.DEBUG,
            exceptionLogLevel = Loggable.LogLevel.WARN,
            logExecutionTime = false,
            logExit = false
    )
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
