package de.unistuttgart.iste.meitrex.gamification_service.dapr;

import de.unistuttgart.iste.meitrex.common.event.MediaRecordInfoEvent;
import de.unistuttgart.iste.meitrex.common.event.MediaRecordWorkedOnEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.PersistentMediaRecordWorkedOnEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentMediaRecordInfoEvent;
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
public class MediaRecordWorkedOnEventListener extends AbstractExternalListener<MediaRecordWorkedOnEvent> {

    private static String getContext(CloudEvent<MediaRecordWorkedOnEvent> cloudEvent) {
        return "";
    }

    public MediaRecordWorkedOnEventListener(
            @Autowired ITimeService timeService,
            @Autowired IEventPublicationService eventService,
            @Value("${de.unistuttgart.iste.meitrex.gamification_service.dapr.log_headers:false}") boolean logHeaders) {
        super(timeService, eventService, logHeaders);
    }

    @Transactional
    @Topic(name = "media-record-worked-on", pubsubName = "meitrex")
    @PostMapping(path = "/media-record-worked-on-pubsub")
    public void onChapterCompletedEvent(@RequestBody CloudEvent<MediaRecordWorkedOnEvent> event, @RequestHeader Map<String, String> headers) {
        System.out.println("media-record-worked-on");
        super.handle(event, MediaRecordWorkedOnEventListener::getContext, headers);
    }

    @Override
    protected PersistentEvent mapToPersistentEvent(MediaRecordWorkedOnEvent event) {
        final PersistentMediaRecordWorkedOnEvent persistentMediaRecordWorkedOnEvent = new PersistentMediaRecordWorkedOnEvent();
        persistentMediaRecordWorkedOnEvent.setUserId(event.getUserId());
        persistentMediaRecordWorkedOnEvent.setMediaRecordId(event.getMediaRecordId());
        return persistentMediaRecordWorkedOnEvent;
    }
}
