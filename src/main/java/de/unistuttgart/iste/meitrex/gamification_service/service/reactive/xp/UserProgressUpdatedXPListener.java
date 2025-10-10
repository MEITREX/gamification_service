package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.xp;

import de.unistuttgart.iste.meitrex.gamification_service.aspects.logging.Loggable;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import org.springframework.stereotype.*;
import org.springframework.context.event.*;
import org.springframework.beans.factory.annotation.*;

import de.unistuttgart.iste.meitrex.gamification_service.time.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.*;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

//@Component
class UserProgressUpdatedXPListener extends AbstractInternalListener<PersistentUserProgressUpdatedEvent,  InternalUserProgressUpdatedEvent> {

    private static final String ERR_MSG_NEW_XP_VALUE_CANT_BE_LESS = "The overall xp value must be non-negative.";

    // Do not change to keep unique UUID even in case of refactoring.
    private static final String name = "UserProgressUpdatedXPListener ";

    private final IUserCreator userCreator;

    public UserProgressUpdatedXPListener(
            @Autowired IPersistentUserProgressUpdatedRepository persistentEventRepository,
            @Autowired IPersistentEventStatusRepository eventStatusRepository,
            @Autowired ITimeService timeService,
            @Autowired IUserCreator userCreator
    ) {
        super(persistentEventRepository, eventStatusRepository, timeService);
        this.userCreator = Objects.requireNonNull(userCreator);
    }

    @Override
    @EventListener
    public void process(InternalUserProgressUpdatedEvent internalEvent) {
        super.process(internalEvent);
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    public void doProcess(PersistentUserProgressUpdatedEvent persistentEvent)
            throws TransientEventListenerException, NonTransientEventListenerException {
        final Optional<Integer> additionalXPOptional = this.mapToAdditionalXP(persistentEvent);
        if(additionalXPOptional.isPresent()) {
            final UUID userID = persistentEvent.getUserId();
            final UserEntity user = this.userCreator.fetchOrCreate(userID);
            final int xpValue = user.getXpValue() + additionalXPOptional.get();
            if(xpValue < 0) {
                throw new NonTransientEventListenerException(ERR_MSG_NEW_XP_VALUE_CANT_BE_LESS);
            }
            user.setXpValue(xpValue);
        }
    }

    private Optional<Integer> mapToAdditionalXP(PersistentUserProgressUpdatedEvent persistentEvent) {
        return Optional.of(10);
    }
}
