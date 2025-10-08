package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.items;

import de.unistuttgart.iste.meitrex.gamification_service.events.internal.AbstractInternalListener;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.InternalSubmissionCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.NonTransientEventListenerException;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.TransientEventListenerException;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentSubmissionCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.IItemService;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InternalSubmissionCompletedEventListener extends AbstractInternalListener<PersistentSubmissionCompletedEvent, InternalSubmissionCompletedEvent> {

    private final IItemService itemService;

    private static final String name = "InternalSubmissionCompletedEventListener";

    public InternalSubmissionCompletedEventListener(@Autowired IPersistentEventRepository<PersistentSubmissionCompletedEvent> persistentEventRepository,
                                                    @Autowired IPersistentEventStatusRepository eventStatusRepository,
                                                    @Autowired ITimeService timeService,
                                                    @Autowired IItemService itemService) {
        super(persistentEventRepository, eventStatusRepository, timeService);
        this.itemService = itemService;
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    @EventListener
    protected void doProcess(PersistentSubmissionCompletedEvent persistentEvent) throws TransientEventListenerException, NonTransientEventListenerException {
        UUID userId = persistentEvent.getUserId();
        itemService.submissionReward(userId);
    }
}
