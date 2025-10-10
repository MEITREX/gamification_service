package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.xp;

import de.unistuttgart.iste.meitrex.common.event.MediaType;
import de.unistuttgart.iste.meitrex.gamification_service.aspects.logging.Loggable;
import de.unistuttgart.iste.meitrex.gamification_service.events.PersistentMediaRecordWorkedOnEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentMediaRecordInfoEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentMediaRecordInfoRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentMediaRecordWorkedOnRepository;
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
public class MediaRecordWorkedOnXPListener extends AbstractInternalListener<PersistentMediaRecordWorkedOnEvent, InternalMediaWorkedOnEvent> {

    // Do not change to keep unique UUID even in case of refactoring.
    private static final String name = "MediaRecordWorkedOnXPListener ";

    private final IUserCreator userCreator;

    private final IUserXPAdder xpAdder;

    private final IPersistentMediaRecordInfoRepository mediaRecordInfoRepository;

    public MediaRecordWorkedOnXPListener(
            @Autowired IPersistentMediaRecordWorkedOnRepository persistentEventRepository,
            @Autowired IPersistentMediaRecordInfoRepository mediaRecordInfoRepository,
            @Autowired IPersistentEventStatusRepository eventStatusRepository,
            @Autowired ITimeService timeService,
            @Autowired IUserCreator userCreator,
            @Autowired IUserXPAdder userXPAdder
    ) {
        super(persistentEventRepository, eventStatusRepository, timeService);
        this.mediaRecordInfoRepository = mediaRecordInfoRepository;
        this.userCreator = Objects.requireNonNull(userCreator);
        this.xpAdder = Objects.requireNonNull(userXPAdder);
    }

    @Override
    @EventListener
    public void process(InternalMediaWorkedOnEvent internalEvent) {
        super.process(internalEvent);
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    public void doProcess(PersistentMediaRecordWorkedOnEvent persistentEvent)
            throws TransientEventListenerException, NonTransientEventListenerException {
        final UserEntity userEntity = this.userCreator.fetchOrCreate(persistentEvent.getUserId());
        final PersistentMediaRecordInfoEvent mediaRecordInfoEvent = findMediaRecordInfo(persistentEvent);
        final MediaType mediaType = mediaRecordInfoEvent.getMediaType();
        if(Objects.nonNull(mediaType)) {
            switch (mediaType) {
                case VIDEO: {
                    this.xpAdder.add(userEntity, IUserXPAdder.Cause.VIDEO_WATCHED, Math.round(mediaRecordInfoEvent.getDurationInSeconds() / 60));
                    break;
                }
                case DOCUMENT:  {
                    this.xpAdder.add(userEntity, IUserXPAdder.Cause.DOCUMENT_OPENED, mediaRecordInfoEvent.getPageCount());
                    break;
                }
            }
        }
    }

    private PersistentMediaRecordInfoEvent findMediaRecordInfo(PersistentMediaRecordWorkedOnEvent persistentEvent) {
        final UUID mediaRecordID = persistentEvent.getMediaRecordId();
        if(Objects.isNull(mediaRecordID)) {
            throw new NonTransientEventListenerException();
        }
        final Optional<PersistentMediaRecordInfoEvent> mediaRecordInfoEvent
                = this.mediaRecordInfoRepository.findByMediaRecordId(mediaRecordID);
        if(mediaRecordInfoEvent.isEmpty()) {
            throw new TransientEventListenerException();
        }
        return mediaRecordInfoEvent.get();
    }

}
