package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.xp;

import de.unistuttgart.iste.meitrex.common.event.ContentProgressedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.aspects.logging.Loggable;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentContentProgressedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentContentProgressedRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserXPAdder;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ContentProgressedXPListener extends AbstractInternalListener<PersistentContentProgressedEvent, InternalContentProgressedEvent> {

    private static int countResponses(PersistentContentProgressedEvent event) {
        if((!event.isSuccess()) || Objects.isNull(event.getResponses())) {
            return 0;
        }
        else {
            return event.getResponses().size();
        }
    }

    private static int countResponsesWithMinimumHalfResponse(PersistentContentProgressedEvent event) {
        if(Objects.nonNull(event.getResponses())) {
            return event.getResponses().stream().filter(persistentResponse
                    -> persistentResponse.getResponse() >= 0.5).toList().size();
        }
        return 0;
    }

    // Do not change to keep unique UUID even in case of refactoring.
    private static final String name = "ChapterCompletionXPListener";

    private final IUserCreator userCreator;

    private final IUserXPAdder userXPAdder;

    public ContentProgressedXPListener(
            @Autowired IPersistentContentProgressedRepository persistentEventRepository,
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
    public void process(InternalContentProgressedEvent internalEvent) {
        super.process(internalEvent);
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    public void doProcess(PersistentContentProgressedEvent persistentEvent)
            throws TransientEventListenerException, NonTransientEventListenerException {
        final UserEntity userEntity = this.userCreator.fetchOrCreate(persistentEvent.getUserId());
        final ContentProgressedEvent.ContentType contentType = persistentEvent.getContentType();
        if(Objects.nonNull(contentType)) {
            switch (contentType) {
                case FLASHCARDS: {
                    userXPAdder.add(userEntity, IUserXPAdder.Cause.FLASHCARD_COMPLETED, countResponses(persistentEvent));
                    break;
                }
                case QUIZ: {
                    userXPAdder.add(userEntity, IUserXPAdder.Cause.QUIZ_COMPLETED, countResponses(persistentEvent));
                    break;
                }
                case ASSIGNMENT: {
                    userXPAdder.add(userEntity, IUserXPAdder.Cause.ASSIGNMENT_COMPLETED);
                    break;
                }
                case SUBMISSION: {
                    userXPAdder.add(userEntity,  IUserXPAdder.Cause.SUBMISSION_COMPLETED,  countResponsesWithMinimumHalfResponse(persistentEvent));
                    break;
                }
            }
        }
    }
}
