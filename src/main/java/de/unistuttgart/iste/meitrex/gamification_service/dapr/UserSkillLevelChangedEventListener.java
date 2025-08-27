package de.unistuttgart.iste.meitrex.gamification_service.dapr;

import de.unistuttgart.iste.meitrex.common.event.UserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.common.event.skilllevels.UserSkillLevelChangedEvent;
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

import java.util.Map;

public class UserSkillLevelChangedEventListener extends AbstractExternalListener<UserSkillLevelChangedEvent> {

    private static String getContext(CloudEvent<UserSkillLevelChangedEvent> cloudEvent) {
        return "";
    }

    public UserSkillLevelChangedEventListener(
            @Autowired ITimeService timeService,
            @Autowired IEventPublicationService eventService,
            @Value("${de.unistuttgart.iste.meitrex.gamification_service.dapr.log_headers:false}") boolean logHeaders
    ) {
        super(timeService, eventService, logHeaders);
    }

    @Transactional
    @PostMapping(path = "/user-skill-level-changed")
    @Topic(name = "user-skill-level-changed", pubsubName = "meitrex")
    public void onUserProgressUpdated(@RequestBody CloudEvent<UserSkillLevelChangedEvent> cloudEvent, @RequestHeader Map<String, String> headers) {
        super.handle(cloudEvent, UserSkillLevelChangedEventListener::getContext, headers);
    }

    @Override
    protected PersistentEvent mapToPersistentEvent(UserSkillLevelChangedEvent event) {

        return null;
    }
}
