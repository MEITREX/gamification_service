package de.unistuttgart.iste.meitrex.gamification_service.dapr;

import de.unistuttgart.iste.meitrex.common.event.ContentProgressedEvent;
import de.unistuttgart.iste.meitrex.common.event.Response;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentContentProgressedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
public class ContentProgressedEventListener extends AbstractExternalListener<ContentProgressedEvent> {

    private static String getContext(CloudEvent<ContentProgressedEvent> cloudEvent) {
        return "";
    }

    public ContentProgressedEventListener(
            @Autowired ITimeService timeService,
            @Autowired IEventPublicationService eventService,
            @Value("${de.unistuttgart.iste.meitrex.gamification_service.dapr.log_headers:false}") boolean logHeaders
    ) {
        super(timeService, eventService, logHeaders);
    }

    @Transactional
    @Topic(name = "content-progressed", pubsubName = "meitrex")
    @PostMapping(path = "/content-progressed-pubsub")
    public void onUserProgressUpdated(@RequestBody CloudEvent<ContentProgressedEvent> cloudEvent, @RequestHeader Map<String, String> headers) {
        super.handle(cloudEvent, ContentProgressedEventListener::getContext, headers);
    }

    @Override
    protected PersistentEvent mapToPersistentEvent(ContentProgressedEvent event) {
        final PersistentContentProgressedEvent persistentContentProgressedEvent = new PersistentContentProgressedEvent();
        persistentContentProgressedEvent.setUserId(event.getUserId());
        persistentContentProgressedEvent.setContentId(event.getContentId());
        persistentContentProgressedEvent.setSuccess(event.isSuccess());
        persistentContentProgressedEvent.setCorrectness(event.getCorrectness());
        persistentContentProgressedEvent.setHintsUsed(event.getHintsUsed());
        persistentContentProgressedEvent.setContentType(event.getContentType());
        final List<Response> responseList = event.getResponses();
        if(Objects.nonNull(responseList)) {
            persistentContentProgressedEvent.setResponses(responseList.stream().map(response -> {
                final PersistentContentProgressedEvent.PersistentResponse persistentResponse
                        = new PersistentContentProgressedEvent.PersistentResponse();
                persistentResponse.setResponseId(response.getItemId());
                persistentResponse.setResponse(response.getResponse());
                persistentResponse.setEvent(persistentContentProgressedEvent);
                persistentContentProgressedEvent.getResponses().add(persistentResponse);
                return persistentResponse;
            }).toList());
        }
        return persistentContentProgressedEvent;
    }
}
