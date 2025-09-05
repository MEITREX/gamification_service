package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.goals;

import de.unistuttgart.iste.meitrex.gamification_service.events.internal.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentChapterCompletedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentChapterCompletedEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.CompletedSpecificChapterGoalProgressEvent;
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
public class ChapterCompletedEventGoalListener extends AbstractInternalListener<PersistentChapterCompletedEvent, InternalChapterCompletedEvent> {

    private final ICourseCreator courseCreator;
    private final IUserCreator userCreator;
    private final ICourseMembershipHandler courseMembershipHandler;
    private final IGoalProgressUpdater goalProgressUpdater;

    @Override
    protected String getName() {
        return "ChapterCompletedEventGoalListener";
    }

    public ChapterCompletedEventGoalListener(
            @Autowired IPersistentChapterCompletedEventRepository persistentEventRepository,
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
    public void process(InternalChapterCompletedEvent internalEvent) {
        super.process(internalEvent);
    }

    @Override
    protected void doProcess(PersistentChapterCompletedEvent persistentEvent) throws TransientEventListenerException, NonTransientEventListenerException {
        UUID userId = persistentEvent.getUserId();

        CourseEntity courseEntity = courseCreator.fetchOrCreate(persistentEvent.getCourseId());
        UserEntity userEntity = userCreator.fetchOrCreate(persistentEvent.getUserId());

        courseMembershipHandler.addUserToCourseIfNotAlready(courseEntity, userEntity);

        GoalProgressEvent goalProgressEvent = CompletedSpecificChapterGoalProgressEvent
                .builder()
                .userId(userId)
                .chapterId(persistentEvent.getChapterId())
                .build();
        this.goalProgressUpdater.updateGoalProgressEntitiesForUser(userEntity, courseEntity.getId(), goalProgressEvent);
    }
}
