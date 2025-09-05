package de.unistuttgart.iste.meitrex.gamification_service.dapr;

import de.unistuttgart.iste.meitrex.common.event.CourseCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentCourseCompletedEvent;
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
public class CourseCompletedEventListener extends AbstractExternalListener<CourseCompletedEvent> {

    private static String getContext(CloudEvent<CourseCompletedEvent> cloudEvent) {
        return "";
    }

    public CourseCompletedEventListener(
            @Autowired ITimeService timeService,
            @Autowired IEventPublicationService eventService,
            @Value("${de.unistuttgart.iste.meitrex.gamification_service.dapr.log_headers:false}") boolean logHeaders) {
        super(timeService, eventService, logHeaders);
    }

    @Transactional
    @Topic(name = "course-completed", pubsubName = "meitrex")
    @PostMapping("/course-completed-pubsub")
    public void onCourseCompletedEvent(@RequestBody CloudEvent<CourseCompletedEvent> event,
                                       @RequestHeader Map<String, String> headers) {
        super.handle(event, CourseCompletedEventListener::getContext, headers);
    }

    @Override
    protected PersistentEvent mapToPersistentEvent(CourseCompletedEvent event) {
        final PersistentCourseCompletedEvent persistentEvent = new PersistentCourseCompletedEvent();
        persistentEvent.setUserId(event.getUserId());
        persistentEvent.setCourseId(event.getCourseId());
        return persistentEvent;
    }
}
