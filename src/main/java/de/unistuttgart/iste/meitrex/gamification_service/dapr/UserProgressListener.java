package de.unistuttgart.iste.meitrex.gamification_service.dapr;


import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.publication.IEventPublicationService;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;


@RestController
public class UserProgressListener {

    private final ITimeService timeService;

    private final IEventPublicationService eventService;

    public UserProgressListener(@Autowired ITimeService timeService, @Autowired IEventPublicationService eventService) {
        this.timeService = Objects.requireNonNull(timeService);
        this.eventService = Objects.requireNonNull(eventService);
    }

    @GetMapping("/test")
    @Transactional
    public void test() {
        this.onUserProgressUpdated(UUID.randomUUID(), UUID.randomUUID(), 3, 0.3);
    }

    @Transactional
    @GetMapping("/test_fixed_course")
    public void testFixedCourseId( @RequestParam int attempt, @RequestParam double correctness) {
        final UUID courseID = UUID.nameUUIDFromBytes("course".getBytes(StandardCharsets.UTF_8));
        onUserProgressUpdated(UUID.randomUUID(), courseID, attempt, correctness);
    }

    @Transactional
    @GetMapping("/test_fixed_user")
    public void testFixedUserId(@RequestParam int attempt, @RequestParam double correctness) {
        final UUID userId = UUID.nameUUIDFromBytes("user".getBytes(StandardCharsets.UTF_8));
        onUserProgressUpdated(userId, UUID.randomUUID(), attempt, correctness);
    }

    @Transactional
    @GetMapping("/test_fixed_user_course")
    public void testFixedCourseUserId(@RequestParam int attempt, @RequestParam double correctness) {
        final UUID userId = UUID.nameUUIDFromBytes("user".getBytes(StandardCharsets.UTF_8));
        final UUID courseID = UUID.nameUUIDFromBytes("course".getBytes(StandardCharsets.UTF_8));
        onUserProgressUpdated(userId, courseID, attempt, correctness);
    }

    public  void onUserProgressUpdated(
            UUID userId,
            UUID courseId,
            Integer userAttempt,
            Double correctness
    ) {
        PersistentUserProgressUpdatedEvent event = this.createUserProgressUpdatedEvent(userId, courseId, userAttempt, correctness);
        this.eventService.saveCommitAndPublish(event);
    }

    private PersistentUserProgressUpdatedEvent createUserProgressUpdatedEvent(
            UUID userId,
            UUID courseId,
            Integer userAttempt,
            Double correctness
    ) {
        final PersistentUserProgressUpdatedEvent event = new PersistentUserProgressUpdatedEvent();
        {
            // Domain-agnostic attributes
            event.setStatus(PersistentEvent.Status.RECEIVED);
            event.setAttemptCount(0);
            event.setMaxCount(3);
            event.setReceivedTimestamp(this.timeService.now());
            // Domain-specific attributes
            event.setUserId(userId);
            event.setCourseId(courseId);
            event.setUserAttempt(userAttempt);
            event.setCorrectness(correctness);
            event.setLastProcessingAttemptTimestamp(System.currentTimeMillis());
        }
        return event;
    }

}
