package de.unistuttgart.iste.meitrex.gamification_service.service.reactive;

import de.unistuttgart.iste.meitrex.gamification_service.events.internal.AbstractInternalListener;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.InternalUserCourseMembershipChangedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.NonTransientEventListenerException;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.TransientEventListenerException;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserCourseMembershipChangedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentUserCourseMembershipChangedEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseMembershipHandler;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserCourseMembershipChangedEventListener
        extends AbstractInternalListener<PersistentUserCourseMembershipChangedEvent, InternalUserCourseMembershipChangedEvent> {

    private final IUserCreator userCreator;
    private final ICourseCreator courseCreator;
    private final ICourseMembershipHandler courseMembershipHandler;

    @Override
    protected String getName() {
        return "UserCourseMembershipChangedEventListener";
    }

    public UserCourseMembershipChangedEventListener(
            @Autowired IPersistentUserCourseMembershipChangedEventRepository persistentEventRepository,
            @Autowired IPersistentEventStatusRepository eventStatusRepository,
            @Autowired ITimeService timeService,
            @Autowired IUserCreator userCreator,
            @Autowired ICourseCreator courseCreator,
            @Autowired ICourseMembershipHandler courseMembershipHandler) {
        super(persistentEventRepository, eventStatusRepository, timeService);
        this.userCreator = userCreator;
        this.courseCreator = courseCreator;
        this.courseMembershipHandler = courseMembershipHandler;
    }

    @EventListener
    @Override
    public void process(InternalUserCourseMembershipChangedEvent internalEvent) {
        super.process(internalEvent);
    }

    @Override
    protected void doProcess(PersistentUserCourseMembershipChangedEvent persistentEvent) throws TransientEventListenerException, NonTransientEventListenerException {
        // if user was previously not a member of the course, add them
        if(persistentEvent.getPreviousRole() == null) {
            UUID courseId = persistentEvent.getCourseId();
            CourseEntity courseEntity = courseCreator.fetchOrCreate(courseId);

            UUID userId = persistentEvent.getUserId();
            UserEntity user = userCreator.fetchOrCreate(userId);

            this.courseMembershipHandler.addUserToCourseIfNotAlready(courseEntity, user);
        }
    }
}
