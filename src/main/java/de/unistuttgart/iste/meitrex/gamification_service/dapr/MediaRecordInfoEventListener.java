package de.unistuttgart.iste.meitrex.gamification_service.dapr;

import de.unistuttgart.iste.meitrex.common.event.ChapterCompletedEvent;
import de.unistuttgart.iste.meitrex.common.event.MediaRecordInfoEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentChapterCompletedEvent;
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
public class MediaRecordInfoEventListener extends AbstractExternalListener<MediaRecordInfoEvent> {

    private static String getContext(CloudEvent<MediaRecordInfoEvent> cloudEvent) {
        return "";
    }

    public MediaRecordInfoEventListener(
            @Autowired ITimeService timeService,
            @Autowired IEventPublicationService eventService,
            @Value("${de.unistuttgart.iste.meitrex.gamification_service.dapr.log_headers:false}") boolean logHeaders) {
        super(timeService, eventService, logHeaders);
    }

    @Transactional
    @Topic(name = "media-record-info", pubsubName = "meitrex")
    @PostMapping(path = "/media-record-info-pubsub")
    public void onChapterCompletedEvent(@RequestBody CloudEvent<MediaRecordInfoEvent> event, @RequestHeader Map<String, String> headers) {
        super.handle(event, MediaRecordInfoEventListener::getContext, headers);
    }

    @Override
    protected PersistentEvent mapToPersistentEvent(MediaRecordInfoEvent event) {
        final PersistentMediaRecordInfoEvent persistentMediaRecordInfoEvent = new PersistentMediaRecordInfoEvent();
        persistentMediaRecordInfoEvent.setMediaRecordId(event.getMediaRecordId());
        persistentMediaRecordInfoEvent.setMediaType(event.getMediaType());
        persistentMediaRecordInfoEvent.setPageCount(event.getPageCount());
        persistentMediaRecordInfoEvent.setDurationInSeconds(event.getDurationSeconds());
        return persistentMediaRecordInfoEvent;
    }
}
