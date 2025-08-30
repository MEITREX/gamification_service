package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.achievements;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.AnswerForumGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.GoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseMembershipHandler;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements.IGoalProgressUpdater;
import org.springframework.context.event.*;
import org.springframework.beans.factory.annotation.*;

import de.unistuttgart.iste.meitrex.gamification_service.time.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.*;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;

@Component
class ForumActivityEventAchievementListener extends AbstractInternalListener<PersistentForumActivityEvent, InternalForumActivityEvent> {

    // Do not change to keep unique UUID even in case of refactoring.
    private static final String name = "ForumActivityListener";


    private final ICourseCreator courseCreator;

    private final IUserCreator userCreator;

    private final ICourseMembershipHandler courseMembershipHandler;

    private final IGoalProgressUpdater goalProgressUpdater;

    public ForumActivityEventAchievementListener(
            @Autowired IPersistentForumActivityRepository persistentEventRepository,
            @Autowired  IPersistentEventStatusRepository eventStatusRepository,
            @Autowired  ITimeService timeService,
            @Autowired ICourseCreator courseCreator,
            @Autowired IUserCreator userCreator,
            @Autowired ICourseMembershipHandler courseMembershipHandler,
            @Autowired  IGoalProgressUpdater goalProgressUpdater
    ) {
        super(persistentEventRepository, eventStatusRepository, timeService);
        this.courseCreator = Objects.requireNonNull(courseCreator);
        this.userCreator = Objects.requireNonNull(userCreator);
        this.courseMembershipHandler = Objects.requireNonNull(courseMembershipHandler);
        this.goalProgressUpdater = Objects.requireNonNull(goalProgressUpdater);
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
    protected void doProcess(PersistentForumActivityEvent persistentEvent)
            throws TransientEventListenerException, NonTransientEventListenerException {

        UUID courseId = persistentEvent.getCourseId();
        CourseEntity courseEntity = courseCreator.fetchOrCreate(courseId);

        UUID userId = persistentEvent.getUserId();
        UserEntity user = userCreator.fetchOrCreate(userId);

        this.courseMembershipHandler.addUserToCourseIfNotAlready(courseEntity, user);

        if (PersistentForumActivityEvent.Type.ANSWER.equals(persistentEvent.getType())) {
            forumAnswerProgress(user, courseId);
        }
    }


    private void forumAnswerProgress(UserEntity user, UUID courseId) {
        GoalProgressEvent goalProgressEvent = AnswerForumGoalProgressEvent.builder()
                .userId(user.getId())
                .build();
        this.goalProgressUpdater.updateGoalProgressEntitiesForUser(user, courseId, goalProgressEvent);
    }


}
