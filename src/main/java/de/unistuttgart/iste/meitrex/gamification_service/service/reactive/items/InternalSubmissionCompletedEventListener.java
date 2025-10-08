package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.items;

import de.unistuttgart.iste.meitrex.gamification_service.events.internal.AbstractInternalListener;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.InternalSubmissionCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.NonTransientEventListenerException;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.TransientEventListenerException;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentSubmissionCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.service.IItemService;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserXPAdder;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class InternalSubmissionCompletedEventListener extends AbstractInternalListener<PersistentSubmissionCompletedEvent, InternalSubmissionCompletedEvent> {

    private final IItemService itemService;

    private final IUserCreator userCreator;

    private final IUserXPAdder userXPAdder;

    private static final String name = "InternalSubmissionCompletedEventListener";

    public InternalSubmissionCompletedEventListener(@Autowired IPersistentEventRepository<PersistentSubmissionCompletedEvent> persistentEventRepository,
                                                    @Autowired IPersistentEventStatusRepository eventStatusRepository,
                                                    @Autowired ITimeService timeService,
                                                    @Autowired IItemService itemService,
                                                    @Autowired IUserCreator userCreator,
                                                    @Autowired IUserXPAdder userXPAdder) {
        super(persistentEventRepository, eventStatusRepository, timeService);
        this.itemService = itemService;
        this.userCreator = userCreator;
        this.userXPAdder = userXPAdder;
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    @EventListener
    public void process(InternalSubmissionCompletedEvent internalEvent) {
        super.process(internalEvent);
    }

    @Override
    protected void doProcess(PersistentSubmissionCompletedEvent persistentEvent) throws TransientEventListenerException, NonTransientEventListenerException {
        log.info("Process Submission Completed Event");
        UUID userId = persistentEvent.getUserId();
        itemService.submissionReward(userId);
        final UserEntity userEntity = this.userCreator.fetchOrCreate(persistentEvent.getUserId());
        this.userXPAdder.add(userEntity, IUserXPAdder.Cause.SUBMISSION_COMPLETED);
    }
}
