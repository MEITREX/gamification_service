package de.unistuttgart.iste.meitrex.gamification_service.dapr;

import de.unistuttgart.iste.meitrex.common.event.skilllevels.SkillEntityChangedEvent;
import de.unistuttgart.iste.meitrex.common.event.skilllevels.UserSkillLevelChangedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentSkillEntityChangedEvent;
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

public class SkillEntityChangedEventListener extends AbstractExternalListener<SkillEntityChangedEvent> {

    private static String getContext(CloudEvent<SkillEntityChangedEvent> cloudEvent) {
        return "";
    }

    public SkillEntityChangedEventListener(@Autowired ITimeService timeService, @Autowired IEventPublicationService eventService, @Value("${de.unistuttgart.iste.meitrex.gamification_service.dapr.log_headers:false}") boolean logHeaders) {
        super(timeService, eventService, logHeaders);
    }

    @Transactional
    @PostMapping(path = "/skill-entity-changed")
    @Topic(name = "skill-entity-changed", pubsubName = "meitrex")
    public void onUserProgressUpdated(@RequestBody CloudEvent<SkillEntityChangedEvent> cloudEvent, @RequestHeader Map<String, String> headers) {
        super.handle(cloudEvent, SkillEntityChangedEventListener::getContext, headers);
    }

    @Override
    protected PersistentEvent mapToPersistentEvent(SkillEntityChangedEvent event) {
        PersistentSkillEntityChangedEvent persistentEvent = new PersistentSkillEntityChangedEvent();
        persistentEvent.setSkillId(event.getSkillId());
        persistentEvent.setSkillName(event.getSkillName());
        persistentEvent.setSkillCategory(event.getSkillCategory());
        persistentEvent.setOperation(event.getOperation());
        return persistentEvent;
    }
}
