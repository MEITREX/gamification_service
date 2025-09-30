package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.xp;

import de.unistuttgart.iste.meitrex.gamification_service.events.internal.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentCourseCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentStageCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentCourseCompletedEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentStageCompletedEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserXPAdder;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CourseCompletionXPListener extends AbstractInternalListener<PersistentCourseCompletedEvent, InternalCourseCompletedEvent> {

    // Do not change to keep unique UUID even in case of refactoring.
    private static final String name = "CourseCompletionXPListener";

    private final IUserCreator userCreator;

    private final IUserXPAdder userXPAdder;

    public CourseCompletionXPListener(
            @Autowired IPersistentCourseCompletedEventRepository persistentEventRepository,
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
    public void process(InternalCourseCompletedEvent internalEvent) {
        super.process(internalEvent);
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    public void doProcess(PersistentCourseCompletedEvent persistentEvent)
            throws TransientEventListenerException, NonTransientEventListenerException {
        final UserEntity userEntity = this.userCreator.fetchOrCreate(persistentEvent.getUserId());
        this.userXPAdder.add(userEntity, IUserXPAdder.Cause.COURSE_COMPLETED);
    }

}
