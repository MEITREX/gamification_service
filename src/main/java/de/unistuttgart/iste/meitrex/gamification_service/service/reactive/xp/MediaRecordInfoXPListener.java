package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.xp;

import de.unistuttgart.iste.meitrex.common.event.MediaType;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentMediaRecordInfoEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentMediaRecordInfoRepository;
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
class MediaRecordInfoXPListener extends AbstractInternalListener<PersistentMediaRecordInfoEvent, InternalMediaRecordInfoEvent> {

    private static final String ERR_MSG_NEW_XP_VALUE_CANT_BE_LESS = "The overall xp value must be non-negative.";

    // Do not change to keep unique UUID even in case of refactoring.
    private static final String name = "MediaRecordInfoXPListener ";

    private final IUserCreator userCreator;

    private final IUserXPAdder xpAdder;

    public MediaRecordInfoXPListener(
            @Autowired IPersistentMediaRecordInfoRepository persistentEventRepository,
            @Autowired IPersistentEventStatusRepository eventStatusRepository,
            @Autowired ITimeService timeService,
            @Autowired IUserCreator userCreator,
            @Autowired IUserXPAdder userXPAdder
    ) {
        super(persistentEventRepository, eventStatusRepository, timeService);
        this.userCreator = Objects.requireNonNull(userCreator);
        this.xpAdder = Objects.requireNonNull(userXPAdder);
    }

    @Override
    @EventListener
    public void process(InternalMediaRecordInfoEvent internalEvent) {
        super.process(internalEvent);
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    protected void doProcess(PersistentMediaRecordInfoEvent persistentEvent)
            throws TransientEventListenerException, NonTransientEventListenerException {
        final UserEntity userEntity = this.userCreator.fetchOrCreate(persistentEvent.getUuid());
        final MediaType mediaType = persistentEvent.getMediaType();
        if(Objects.nonNull(mediaType)) {
            switch (mediaType) {
                case VIDEO: {
                    this.xpAdder.add(userEntity, IUserXPAdder.Cause.VIDEO_WATCHED, Math.round(persistentEvent.getDurationInSeconds() / 60));
                    break;
                }
                case DOCUMENT:  {
                    this.xpAdder.add(userEntity, IUserXPAdder.Cause.DOCUMENT_OPENED, persistentEvent.getPageCount());
                    break;
                }
            }
        }
    }

    private Optional<Integer> mapToAdditionalXP(PersistentUserProgressUpdatedEvent persistentEvent) {
        return Optional.of(10);
    }
}
