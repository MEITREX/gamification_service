package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.goals;

import de.unistuttgart.iste.meitrex.gamification_service.events.internal.AbstractInternalListener;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.InternalAskedTutorAQuestionEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.NonTransientEventListenerException;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.TransientEventListenerException;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentAskedTutorAQuestionEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AskedTutorAQuestionEventGoalListener
        extends AbstractInternalListener<PersistentAskedTutorAQuestionEvent, InternalAskedTutorAQuestionEvent> {

    @Override
    protected String getName() {
        return "AskedTutorAQuestionEventGoalListener";
    }

    public AskedTutorAQuestionEventGoalListener(@Autowired IPersistentEventRepository<PersistentAskedTutorAQuestionEvent> persistentEventRepository,
                                                @Autowired IPersistentEventStatusRepository eventStatusRepository,
                                                @Autowired ITimeService timeService) {
        super(persistentEventRepository, eventStatusRepository, timeService);
    }

    @Override
    @EventListener
    public void process(InternalAskedTutorAQuestionEvent internalEvent) {
        super.process(internalEvent);
    }

    @Override
    protected void doProcess(PersistentAskedTutorAQuestionEvent persistentEvent) throws TransientEventListenerException, NonTransientEventListenerException {

    }
}
