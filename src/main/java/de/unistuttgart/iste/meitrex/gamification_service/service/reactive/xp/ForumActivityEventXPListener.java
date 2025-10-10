package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.xp;

import de.unistuttgart.iste.meitrex.common.event.ForumActivityEvent;
import de.unistuttgart.iste.meitrex.gamification_service.aspects.logging.Loggable;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.AbstractInternalListener;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.InternalForumActivityEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.NonTransientEventListenerException;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.TransientEventListenerException;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentForumActivityEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentForumActivityRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.AnswerForumGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseMembershipHandler;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserXPAdder;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements.IGoalProgressUpdater;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
public class ForumActivityEventXPListener extends AbstractInternalListener<PersistentForumActivityEvent, InternalForumActivityEvent> {

    // Do not change to keep unique UUID even in case of refactoring.
    private static final String name = "ForumActivityListener";

    private final IUserCreator userCreator;

    private final IUserXPAdder userXPAdder;

    public ForumActivityEventXPListener(
            @Autowired IPersistentForumActivityRepository persistentEventRepository,
            @Autowired  IPersistentEventStatusRepository eventStatusRepository,
            @Autowired  ITimeService timeService,
            @Autowired IUserXPAdder userXPAdder,
            @Autowired IUserCreator userCreator
    ) {
        super(persistentEventRepository, eventStatusRepository, timeService);
        this.userCreator = Objects.requireNonNull(userCreator);
        this.userXPAdder = Objects.requireNonNull(userXPAdder);
    }


    @Override
    @EventListener
    public void process(InternalForumActivityEvent internalEvent) {
        super.process(internalEvent);
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    public void doProcess(PersistentForumActivityEvent persistentEvent)
            throws TransientEventListenerException, NonTransientEventListenerException {
        final UserEntity author = this.userCreator.fetchOrCreate(persistentEvent.getUserId());
        switch (persistentEvent.getType()) {
            case INFO, THREAD, QUESTION: {
                this.userXPAdder.add(author, IUserXPAdder.Cause.NEW_FORUM_POST);
                break;
            }
            case ANSWER_ACCEPTED: {
                this.userXPAdder.add(author, IUserXPAdder.Cause.ANSWER_ACCEPTED);
                break;
            }
        }
    }

}
