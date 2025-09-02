package de.unistuttgart.iste.meitrex.gamification_service.dapr;

import de.unistuttgart.iste.meitrex.common.event.StageCompletedEvent;
import de.unistuttgart.iste.meitrex.common.event.skilllevels.SkillEntityChangedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentStageCompletedEvent;
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
public class StageCompletedEventListener extends AbstractExternalListener<StageCompletedEvent> {

    private static String getContext(CloudEvent<StageCompletedEvent> cloudEvent) {
        return "";
    }

    public StageCompletedEventListener(
            @Autowired ITimeService timeService,
            @Autowired IEventPublicationService eventService,
            @Value("${de.unistuttgart.iste.meitrex.gamification_service.dapr.log_headers:false}") boolean logHeaders) {
        super(timeService, eventService, logHeaders);
    }

    @Transactional
    @Topic(name = "stage-completed", pubsubName = "meitrex")
    @PostMapping(path = "/stage-completed-pubsub")
    public void onStageCompletedEvent(@RequestBody CloudEvent<StageCompletedEvent> event,
                                      @RequestHeader Map<String, String> headers) {
        super.handle(event, StageCompletedEventListener::getContext, headers);
    }

    @Override
    protected PersistentEvent mapToPersistentEvent(StageCompletedEvent event) {
        final PersistentStageCompletedEvent persistentStageCompletedEvent = new PersistentStageCompletedEvent();
        persistentStageCompletedEvent.setCourseId(event.getCourseId());
        persistentStageCompletedEvent.setChapterId(event.getChapterId());
        persistentStageCompletedEvent.setStageId(event.getStageId());
        persistentStageCompletedEvent.setUserId(event.getUserId());
        return persistentStageCompletedEvent;
    }
}
