package de.unistuttgart.iste.meitrex.gamification_service.dapr;

import de.unistuttgart.iste.meitrex.common.event.SubmissionCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentSubmissionCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.publication.IEventPublicationService;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SubmissionCompletedEventListener extends AbstractExternalListener<SubmissionCompletedEvent>{
    private static String getContext(CloudEvent<SubmissionCompletedEvent> cloudEvent) {
        return "";
    }

    public SubmissionCompletedEventListener(
            @Autowired ITimeService timeService,
            @Autowired IEventPublicationService eventService,
            @Value("${de.unistuttgart.iste.meitrex.gamification_service.dapr.log_headers:false}") boolean logHeaders) {
        super(timeService, eventService, logHeaders);
    }

    @Transactional
    @Topic(name = "submission-completed", pubsubName = "meitrex")
    @PostMapping(path = "/submission-completed-pubsub")
    public void onSubmissionCompletedEvent(@RequestBody CloudEvent<SubmissionCompletedEvent> event,
                                           @RequestHeader Map<String, String> headers) {
        super.handle(event, SubmissionCompletedEventListener::getContext, headers);
    }

    @Override
    protected PersistentEvent mapToPersistentEvent(SubmissionCompletedEvent event) {
        final PersistentSubmissionCompletedEvent persistentEvent = new PersistentSubmissionCompletedEvent();
        persistentEvent.setUserId(event.getUserId());
        persistentEvent.setSubmissionId(event.getSubmissionId());
        return persistentEvent;
    }
}
