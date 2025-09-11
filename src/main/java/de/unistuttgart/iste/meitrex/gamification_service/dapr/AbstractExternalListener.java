package de.unistuttgart.iste.meitrex.gamification_service.dapr;

import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.publication.IEventPublicationService;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import io.dapr.client.domain.CloudEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
abstract class AbstractExternalListener<T> {

    private final ITimeService timeService;

    private final IEventPublicationService eventService;

    private final boolean logHeaders;

    public AbstractExternalListener(ITimeService timeService, IEventPublicationService eventService, boolean logHeaders) {
        this.timeService = Objects.requireNonNull(timeService);
        this.eventService = Objects.requireNonNull(eventService);
        this.logHeaders = logHeaders;
    }

    protected final void handle(CloudEvent<T> cloudEvent, Function<CloudEvent<T>, String> contextProvider, Map<String, String> headers) {
        Objects.requireNonNull(cloudEvent);
        Objects.requireNonNull(contextProvider);
        System.out.println("media-record-worked-on-1");
        try {
            logEntry(cloudEvent, contextProvider, headers);
            final T body = cloudEvent.getData();
            if(Objects.isNull(body)) {
                logEmptyBody(cloudEvent, headers);
                return;
            }
            System.out.println("media-record-worked-on-2");
            final PersistentEvent persistentEvent = setDeliveryDetails(this.mapToPersistentEvent(cloudEvent.getData()));
            System.out.println("media-record-worked-on-3");

            this.eventService.saveCommitAndPublishIfNew(persistentEvent);
        } catch(RuntimeException e0) {
            logException(cloudEvent, e0, contextProvider, headers);
            throw e0;
        }
    }

    private PersistentEvent setDeliveryDetails(PersistentEvent persistentEvent) {
        final Long curTime = this.timeService.curTime();
        persistentEvent.setReceivedTimestamp(curTime);
        return persistentEvent;
    }

    private void logEntry(CloudEvent<T> cloudEvent, Function<CloudEvent<T>, String> contextProvider, Map<String, String> headers) {
        System.out.println(contextProvider);
    }

    private void logEmptyBody(CloudEvent<T> cloudEvent, Map<String, String> headers) {
        log.warn("Event had empty body. Ignoring. CloudEvent: {}", cloudEvent);
    }

    private void logException(CloudEvent<T> cloudEvent, Exception e, Function<CloudEvent<T>, String> contextProvider, Map<String, String> headers) {
        System.out.println(e.getCause());
    }

    protected abstract PersistentEvent mapToPersistentEvent(T event);

}

