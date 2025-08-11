package de.unistuttgart.iste.meitrex.gamification_service.dapr;


import de.unistuttgart.iste.meitrex.common.event.UserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.publication.IEventPublicationService;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


/**
 * {@link UserProgressListener} serves the detection of publication of instances of {@link UserProgressUpdatedEvent},
 * their persistent logging as well as their propagation in a transactionally isolated manner.
 *
 * @author Philipp Kunz
 */
@Slf4j
@RestController
public class UserProgressListener {

    private static void logEntry(UserProgressUpdatedEvent event) {
        final String ERR_MSG = "Received an instance of {}. ID: {}";
        log.info(ERR_MSG, event.getClass().getName(), event.getUserId());
    }

    private static void logException(UserProgressUpdatedEvent event, Exception e) {
        final String ERR_MSG = "Processing an instance of {} failed. Unable to create a persistent log.";
        log.error(ERR_MSG, event.getClass().getName(), e);
    }


    private final ITimeService timeService;

    private final IEventPublicationService eventService;


    private final int MAX_RETRY_COUNT;

    /**
     * Public default constructor for {@link UserProgressListener}.
     * @param timeService an instance of {@link ITimeService}.
     * @param eventService an instance of {@link IEventPublicationService}.
     * @param maxRetryCount the maximum number of attempts to process a persistently logged instance of {@link UserProgressUpdatedEvent}.
     */
    public UserProgressListener(
            @Autowired ITimeService timeService,
            @Autowired IEventPublicationService eventService,
            @Value("${de.unistuttgart.iste.meitrex.gamification_service.dapr.max_attempt_count: 3}") int maxRetryCount
    ) {
        this.timeService = Objects.requireNonNull(timeService);
        this.eventService = Objects.requireNonNull(eventService);
        this.MAX_RETRY_COUNT = maxRetryCount;
    }

    /**
     * Handles the publication of an instance of {@link UserProgressUpdatedEvent} via the annotated Dapr topic in an
     * idempotent manner.
     *
     * @param cloudEvent an instance of {@link UserProgressUpdatedEvent} to be handled.
     */
    @Transactional
    @PostMapping(path = "/user-progress-updated")
    @Topic(name = "user-progress-updated", pubsubName = "meitrex")
    public Mono<Void> onUserProgressUpdated(@RequestBody(required = false) CloudEvent<UserProgressUpdatedEvent> cloudEvent, @RequestHeader Map<String, String> headers) {
        final UserProgressUpdatedEvent event = cloudEvent.getData();
        try {
            logEntry(event);
            this.eventService.saveCommitAndPublishIfNew(this.createUserProgressUpdatedEvent(event));
        } catch(RuntimeException e0) {
            logException(event, e0);
            throw e0;
        }
        return Mono.empty();
    }

    //@Transactional
    //@Scheduled(fixedRate = 5000)
    public void onStart()
            throws IOException, InterruptedException {

        System.out.println("Executed!!!!");

        UserProgressUpdatedEvent event = UserProgressUpdatedEvent.builder()
                .userId(UUID.randomUUID())
                .contentId(UUID.randomUUID())
                .chapterId(UUID.randomUUID())
                .courseId(UUID.randomUUID())
                .success(true)
                .correctness(0.85)
                .attempt(0)
                .sequenceNo(System.currentTimeMillis())
                .hintsUsed(2)
                .timeToComplete(300)
                .responses(List.of(/* add ItemResponse objects here */))
                .build();

        onUserProgressUpdated(event);


    }



    public Mono<Void> onUserProgressUpdated(UserProgressUpdatedEvent event ) {
        try {
            logEntry(event);
            this.eventService.saveCommitAndPublishIfNew(this.createUserProgressUpdatedEvent(event));
        } catch(RuntimeException e0) {
            logException(event, e0);
            throw e0;
        }
        return Mono.empty();
    }

    /**
     * Converts the passed instance of {@link UserProgressUpdatedEvent} to an instance of {@link PersistentUserProgressUpdatedEvent},
     * which is specifically designed for persistent internal propagation.
     *
     * @return an instance of {@link PersistentUserProgressUpdatedEvent} suitable for internal and transactionally
     * isolated propagation.
     */
    private PersistentUserProgressUpdatedEvent createUserProgressUpdatedEvent(UserProgressUpdatedEvent event) {
        final PersistentUserProgressUpdatedEvent persistentEvent = new PersistentUserProgressUpdatedEvent();
        final long curTime = this.timeService.curTime();
        persistentEvent.setSequenceNo(event.getSequenceNo());
        persistentEvent.setStatus(PersistentEvent.Status.RECEIVED);
        persistentEvent.setAttemptCount(0);
        persistentEvent.setMaxCount(MAX_RETRY_COUNT);
        persistentEvent.setReceivedTimestamp(curTime);
        persistentEvent.setUserId(event.getUserId());
        persistentEvent.setCourseId(event.getCourseId());
        persistentEvent.setContentId(event.getContentId());
        persistentEvent.setCorrectness(event.getCorrectness());
        persistentEvent.setAssessmentAttempt(event.getAttempt());
        persistentEvent.setLastProcessingAttemptTimestamp(curTime);
        return persistentEvent;
    }

}
