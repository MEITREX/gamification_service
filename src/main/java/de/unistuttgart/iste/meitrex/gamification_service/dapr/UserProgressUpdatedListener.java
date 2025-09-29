package de.unistuttgart.iste.meitrex.gamification_service.dapr;

import de.unistuttgart.iste.meitrex.common.event.MediaType;
import de.unistuttgart.iste.meitrex.common.event.UserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.publication.IEventPublicationService;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Working
@RestController
class UserProgressUpdatedListener extends AbstractExternalListener<UserProgressUpdatedEvent> {

    private static String getContext(CloudEvent<UserProgressUpdatedEvent> cloudEvent) {
        return "";
    }

    public UserProgressUpdatedListener(
            @Autowired ITimeService timeService,
            @Autowired IEventPublicationService eventService,
            @Value("${de.unistuttgart.iste.meitrex.gamification_service.dapr.log_headers:false}") boolean logHeaders
    ) {
        super(timeService, eventService, logHeaders);
    }

    @Transactional
    @PostMapping(path = "/user-progress-updated")
    @Topic(name = "user-progress-updated", pubsubName = "meitrex")
    public void onUserProgressUpdated(@RequestBody CloudEvent<UserProgressUpdatedEvent> cloudEvent, @RequestHeader Map<String, String> headers) {
        System.out.println("$ " + this.getClass().getName());
        super.handle(cloudEvent, UserProgressUpdatedListener::getContext, headers);
    }

    @Override
    protected PersistentUserProgressUpdatedEvent mapToPersistentEvent(UserProgressUpdatedEvent event) {
        final PersistentUserProgressUpdatedEvent persistentEvent = new PersistentUserProgressUpdatedEvent();
        persistentEvent.setMsgSequenceNo(event.getSequenceNo());
        persistentEvent.setUserId(event.getUserId());
        persistentEvent.setCourseId(event.getCourseId());
        persistentEvent.setContentId(event.getContentId());
        persistentEvent.setCorrectness(event.getCorrectness());
        persistentEvent.setAssessmentAttempt(event.getAttempt());
        persistentEvent.setChapterId(event.getChapterId());
        return persistentEvent;
    }

    @Transactional
    @GetMapping(path = "/test")
    public void test() {
        CloudEvent<UserProgressUpdatedEvent>  cloudEvent = new CloudEvent<>();
        cloudEvent.setData(new UserProgressUpdatedEvent(
                System.currentTimeMillis(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                0,
                true,
                1.0,
                0,
                0,
                new ArrayList<>(),
                MediaType.VIDEO));
        this.onUserProgressUpdated(cloudEvent, new HashMap<>());
    }
}
