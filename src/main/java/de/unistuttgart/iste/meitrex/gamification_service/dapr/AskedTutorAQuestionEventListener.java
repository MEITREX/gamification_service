package de.unistuttgart.iste.meitrex.gamification_service.dapr;

import de.unistuttgart.iste.meitrex.common.event.AskedTutorAQuestionEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentAskedTutorAQuestionEvent;
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
public class AskedTutorAQuestionEventListener extends AbstractExternalListener<AskedTutorAQuestionEvent> {

    private static String getContext(CloudEvent<AskedTutorAQuestionEvent> cloudEvent) {
        return "";
    }

    public AskedTutorAQuestionEventListener(
            @Autowired ITimeService timeService,
            @Autowired IEventPublicationService eventService,
            @Value("${de.unistuttgart.iste.meitrex.gamification_service.dapr.log_headers:false}") boolean logHeaders) {
        super(timeService, eventService, logHeaders);
    }

    @Transactional
    @Topic(name = "asked-tutor-a-question", pubsubName = "meitrex")
    @PostMapping(path = "/asked-tutor-a-question-pubsub")
    public void onAskedTutorAQuestionEvent(@RequestBody CloudEvent<AskedTutorAQuestionEvent> event,
                                           @RequestHeader Map<String, String> headers) {
        super.handle(event, AskedTutorAQuestionEventListener::getContext, headers);
    }

    @Override
    protected PersistentEvent mapToPersistentEvent(AskedTutorAQuestionEvent event) {
        final PersistentAskedTutorAQuestionEvent persistentEvent = new PersistentAskedTutorAQuestionEvent();
        persistentEvent.setCourseId(event.getCourseId());
        persistentEvent.setUserId(event.getUserId());
        persistentEvent.setQuestion(event.getQuestion());
        persistentEvent.setCategory(event.getCategory());
        return persistentEvent;
    }
}
