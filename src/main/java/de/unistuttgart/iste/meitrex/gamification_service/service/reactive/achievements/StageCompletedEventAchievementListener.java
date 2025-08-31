package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.achievements;

import de.unistuttgart.iste.meitrex.gamification_service.events.internal.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentForumActivityEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentStageCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.CompletedSpecificStageGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
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
public class StageCompletedEventAchievementListener extends AbstractInternalListener<PersistentStageCompletedEvent, InternalStageCompletedEvent> {

    private final ICourseCreator courseCreator;
    private final IUserCreator userCreator;
    private final ICourseMembershipHandler courseMembershipHandler;
    private final IGoalProgressUpdater goalProgressUpdater;

    public StageCompletedEventAchievementListener(
            @Autowired IPersistentEventRepository<PersistentStageCompletedEvent> persistentEventRepository,
            @Autowired IPersistentEventStatusRepository eventStatusRepository,
            @Autowired ITimeService timeService, ICourseCreator courseCreator,
            @Autowired IUserCreator userCreator,
            @Autowired ICourseMembershipHandler courseMembershipHandler,
            @Autowired IGoalProgressUpdater goalProgressUpdater) {
        super(persistentEventRepository, eventStatusRepository, timeService);
        this.courseCreator = courseCreator;
        this.userCreator = userCreator;
        this.courseMembershipHandler = courseMembershipHandler;
        this.goalProgressUpdater = goalProgressUpdater;
    }

    @EventListener
    @Override
    public void process(InternalStageCompletedEvent internalEvent) {
        super.process(internalEvent);
    }

    @Override
    protected String getName() {
        return "StageCompletedListener";
    }

    @Override
    protected void doProcess(PersistentStageCompletedEvent persistentEvent)
            throws TransientEventListenerException, NonTransientEventListenerException {

        UUID courseId = persistentEvent.getCourseId();
        CourseEntity courseEntity = courseCreator.fetchOrCreate(courseId);

        UUID userId = persistentEvent.getUserId();
        UserEntity user = userCreator.fetchOrCreate(userId);

        this.courseMembershipHandler.addUserToCourseIfNotAlready(courseEntity, user);

        GoalProgressEvent goalProgressEvent = CompletedSpecificStageGoalProgressEvent.builder()
                .stageId(persistentEvent.getStageId())
                .build();
        this.goalProgressUpdater.updateGoalProgressEntitiesForUser(user, courseId, goalProgressEvent);
    }
}
