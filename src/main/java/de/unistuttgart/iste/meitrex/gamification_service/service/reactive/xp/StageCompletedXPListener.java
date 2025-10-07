package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.xp;

import de.unistuttgart.iste.meitrex.gamification_service.events.internal.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentStageCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentStageCompletedEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentUserProgressUpdatedRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserXPAdder;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class StageCompletedXPListener extends AbstractInternalListener<PersistentStageCompletedEvent,  InternalStageCompletedEvent> {

    // Do not change to keep unique UUID even in case of refactoring.
    private static final String name = "StageCompletedXPListener";

    private final IUserCreator userCreator;

    private final IUserXPAdder userXPAdder;

    public StageCompletedXPListener(
            @Autowired IPersistentStageCompletedEventRepository persistentEventRepository,
            @Autowired IPersistentEventStatusRepository eventStatusRepository,
            @Autowired ITimeService timeService,
            @Autowired IUserCreator userCreator,
            @Autowired IUserXPAdder userXPAdder
    ) {
        super(persistentEventRepository, eventStatusRepository, timeService);
        this.userCreator = Objects.requireNonNull(userCreator);
        this.userXPAdder = Objects.requireNonNull(userXPAdder);
    }

    @Override
    @EventListener
    public void process(InternalStageCompletedEvent internalEvent) {
        super.process(internalEvent);
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    public void doProcess(PersistentStageCompletedEvent persistentEvent)
            throws TransientEventListenerException, NonTransientEventListenerException {
        final UserEntity userEntity = this.userCreator.fetchOrCreate(persistentEvent.getUserId());
        this.userXPAdder.add(userEntity, IUserXPAdder.Cause.STAGE_COMPLETED);
    }

}
