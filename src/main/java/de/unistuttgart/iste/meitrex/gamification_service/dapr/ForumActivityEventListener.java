package de.unistuttgart.iste.meitrex.gamification_service.dapr;

import de.unistuttgart.iste.meitrex.common.event.ForumActivity;
import de.unistuttgart.iste.meitrex.common.event.ForumActivityEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentForumActivityEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.publication.IEventPublicationService;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import jakarta.transaction.Transactional;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.*;

import java.util.Map;

@RestController
public class ForumActivityEventListener extends AbstractExternalListener<ForumActivityEvent> {

    private static String getContext(CloudEvent<ForumActivityEvent > cloudEvent) {
        return "";
    }

    private static PersistentForumActivityEvent.Type mapForumActivity(ForumActivity forumActivity) {
        if(forumActivity == null) {
            return null;
        }
        return PersistentForumActivityEvent.Type.valueOf(forumActivity.name());
    }

    public ForumActivityEventListener(
            @Autowired ITimeService timeService,
            @Autowired IEventPublicationService eventService,
            @Value("${de.unistuttgart.iste.meitrex.gamification_service.dapr.log_headers:false}") boolean logHeaders
    ) {
        super(timeService, eventService, logHeaders);
    }

    @Transactional
    @Topic(name = "forum-activity", pubsubName = "meitrex")
    @PostMapping(path = "/forum-activity-pubsub")
    public void onUserProgressUpdated(@RequestBody CloudEvent<ForumActivityEvent> cloudEvent, @RequestHeader Map<String, String> headers) {
        super.handle(cloudEvent, ForumActivityEventListener::getContext, headers);
    }

    @Override
    protected PersistentEvent mapToPersistentEvent(ForumActivityEvent event) {
        final PersistentForumActivityEvent persistentForumActivityEvent = new PersistentForumActivityEvent();
        persistentForumActivityEvent.setUserId(event.getUserId());
        persistentForumActivityEvent.setCourseId(event.getForumId());
        persistentForumActivityEvent.setType(mapForumActivity(event.getActivity()));
        return persistentForumActivityEvent;
    }

}