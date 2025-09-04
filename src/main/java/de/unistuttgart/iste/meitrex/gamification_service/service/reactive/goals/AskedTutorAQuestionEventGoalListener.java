package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.goals;

import de.unistuttgart.iste.meitrex.common.event.TutorCategory;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.AbstractInternalListener;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.InternalAskedTutorAQuestionEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.NonTransientEventListenerException;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.TransientEventListenerException;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentAskedTutorAQuestionEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentAskedTutorAQuestionEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.AskedTutorAQuestionGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseMembershipHandler;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements.IGoalProgressUpdater;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AskedTutorAQuestionEventGoalListener
        extends AbstractInternalListener<PersistentAskedTutorAQuestionEvent, InternalAskedTutorAQuestionEvent> {

    private final ICourseCreator courseCreator;
    private final ICourseMembershipHandler courseMembershipHandler;
    private final IUserCreator userCreator;
    private final IGoalProgressUpdater goalProgressUpdater;

    @Override
    protected String getName() {
        return "AskedTutorAQuestionEventGoalListener";
    }

    public AskedTutorAQuestionEventGoalListener(@Autowired IPersistentAskedTutorAQuestionEventRepository persistentEventRepository,
                                                @Autowired IPersistentEventStatusRepository eventStatusRepository,
                                                @Autowired ITimeService timeService,
                                                @Autowired ICourseCreator courseCreator,
                                                @Autowired ICourseMembershipHandler courseMembershipHandler,
                                                @Autowired IUserCreator userCreator,
                                                @Autowired IGoalProgressUpdater goalProgressUpdater) {
        super(persistentEventRepository, eventStatusRepository, timeService);
        this.courseCreator = courseCreator;
        this.courseMembershipHandler = courseMembershipHandler;
        this.userCreator = userCreator;
        this.goalProgressUpdater = goalProgressUpdater;
    }

    @Override
    @EventListener
    public void process(InternalAskedTutorAQuestionEvent internalEvent) {
        super.process(internalEvent);
    }

    @Override
    protected void doProcess(PersistentAskedTutorAQuestionEvent persistentEvent) throws TransientEventListenerException, NonTransientEventListenerException {
        UUID userId = persistentEvent.getUserId();

        if(persistentEvent.getCategory() != TutorCategory.LECTURE)
            return;

        CourseEntity courseEntity = courseCreator.fetchOrCreate(persistentEvent.getCourseId());
        UserEntity userEntity = userCreator.fetchOrCreate(persistentEvent.getUserId());

        courseMembershipHandler.addUserToCourseIfNotAlready(courseEntity, userEntity);

        AskedTutorAQuestionGoalProgressEvent goalProgressEvent = AskedTutorAQuestionGoalProgressEvent.builder()
                .userId(userId)
                .build();
        this.goalProgressUpdater.updateGoalProgressEntitiesForUser(userEntity, courseEntity.getId(), goalProgressEvent);
    }
}
