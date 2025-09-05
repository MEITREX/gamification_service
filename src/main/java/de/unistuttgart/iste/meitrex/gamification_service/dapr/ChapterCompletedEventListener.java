package de.unistuttgart.iste.meitrex.gamification_service.dapr;

import de.unistuttgart.iste.meitrex.common.event.ChapterCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentChapterCompletedEvent;
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

import java.util.Map;

@RestController
public class ChapterCompletedEventListener extends AbstractExternalListener<ChapterCompletedEvent> {

    private static String getContext(CloudEvent<ChapterCompletedEvent> cloudEvent) {
        return "";
    }

    public ChapterCompletedEventListener(
            @Autowired ITimeService timeService,
            @Autowired IEventPublicationService eventService,
            @Value("${de.unistuttgart.iste.meitrex.gamification_service.dapr.log_headers:false}") boolean logHeaders) {
        super(timeService, eventService, logHeaders);
    }

    @Transactional
    @Topic(name = "chapter-completed", pubsubName = "meitrex")
    @PostMapping(path = "/chapter-completed-pubsub")
    public void onChapterCompletedEvent(@RequestBody CloudEvent<ChapterCompletedEvent> event, @RequestHeader Map<String, String> headers) {
        System.out.println(ChapterCompletedEventListener.class.getName() + "$executed");
        super.handle(event, ChapterCompletedEventListener::getContext, headers);
    }

    @Override
    protected PersistentEvent mapToPersistentEvent(ChapterCompletedEvent event) {
        final PersistentChapterCompletedEvent persistentEvent = new PersistentChapterCompletedEvent();
        persistentEvent.setChapterId(event.getChapterId());
        persistentEvent.setCourseId(event.getCourseId());
        persistentEvent.setUserId(event.getUserId());
        return persistentEvent;
    }
}
