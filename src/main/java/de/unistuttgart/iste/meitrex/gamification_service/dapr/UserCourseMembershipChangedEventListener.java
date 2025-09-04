package de.unistuttgart.iste.meitrex.gamification_service.dapr;

import de.unistuttgart.iste.meitrex.common.event.UserCourseMembershipChangedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserCourseMembershipChangedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.publication.IEventPublicationService;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserCourseMembershipChangedEventListener extends AbstractExternalListener<UserCourseMembershipChangedEvent> {

    private static String getContext(CloudEvent<UserCourseMembershipChangedEvent> cloudEvent) {
        return "";
    }

    public UserCourseMembershipChangedEventListener(
            @Autowired ITimeService timeService,
            @Autowired IEventPublicationService eventService,
            @Value("${de.unistuttgart.iste.meitrex.gamification_service.dapr.log_headers:false}") boolean logHeaders) {
        super(timeService, eventService, logHeaders);
    }

    @Transactional
    @Topic(name = "user-course-membership-changed", pubsubName = "meitrex")
    @PostMapping(path = "/user-course-membership-changed-pubsub")
    public void onUserCourseMembershipChangedEvent(CloudEvent<UserCourseMembershipChangedEvent> event) {
        super.handle(event, UserCourseMembershipChangedEventListener::getContext, null);
    }

    @Override
    protected PersistentEvent mapToPersistentEvent(UserCourseMembershipChangedEvent event) {
        PersistentUserCourseMembershipChangedEvent persistentEvent = new PersistentUserCourseMembershipChangedEvent();
        persistentEvent.setUserId(event.getUserId());
        persistentEvent.setCourseId(event.getCourseId());
        persistentEvent.setPreviousRole(event.getPreviousRole());
        persistentEvent.setNewRole(event.getNewRole());
        return persistentEvent;
    }
}
