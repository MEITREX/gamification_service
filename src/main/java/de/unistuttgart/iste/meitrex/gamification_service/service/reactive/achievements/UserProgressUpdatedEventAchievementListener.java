package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.achievements;

import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentContentProgressedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentContentProgressedRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentUserProgressUpdatedRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.CompletedSpecificChapterGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseMembershipHandler;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements.IGoalProgressUpdater;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import de.unistuttgart.iste.meitrex.generated.dto.CompositeProgressInformation;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.util.Objects;
import java.util.UUID;

class UserProgressUpdatedEventAchievementListener extends AbstractInternalListener<PersistentUserProgressUpdatedEvent, InternalUserProgressUpdatedEvent>   {

    // Do not change to keep unique UUID even in case of refactoring.
    private static final String name = "UserProgressUpdatedEventAchievementListener";

    private final ICourseCreator courseCreator;

    private final IUserCreator userCreator;

    private final ContentServiceClient contentServiceClient;

    private final ICourseMembershipHandler courseMembershipHandler;

    private final IGoalProgressUpdater goalProgressUpdater;

    public UserProgressUpdatedEventAchievementListener(
            @Autowired IPersistentUserProgressUpdatedRepository persistentEventRepository,
            @Autowired  IPersistentEventStatusRepository eventStatusRepository,
            @Autowired  ITimeService timeService,
            @Autowired ICourseCreator courseCreator,
            @Autowired IUserCreator userCreator,
            @Autowired ContentServiceClient contentServiceClient,
            @Autowired ICourseMembershipHandler courseMembershipHandler,
            @Autowired IGoalProgressUpdater goalProgressUpdater
    ) {
        super(persistentEventRepository, eventStatusRepository, timeService);
        this.courseCreator = Objects.requireNonNull(courseCreator);
        this.userCreator = Objects.requireNonNull(userCreator);
        this.contentServiceClient = Objects.requireNonNull(contentServiceClient);
        this.courseMembershipHandler = Objects.requireNonNull(courseMembershipHandler);
        this.goalProgressUpdater = Objects.requireNonNull(goalProgressUpdater);
    }

    @Override
    @EventListener
    public void process(InternalUserProgressUpdatedEvent internalEvent) {
        super.process(internalEvent);
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    protected void doProcess(PersistentUserProgressUpdatedEvent persistentEvent)
            throws TransientEventListenerException, NonTransientEventListenerException {


        UUID courseId = persistentEvent.getCourseId();
        CourseEntity courseEntity = courseCreator.fetchOrCreate(courseId);

        UUID userId = persistentEvent.getUserId();
        UserEntity user = userCreator.fetchOrCreate(userId);

        UUID chapterId = persistentEvent.getChapterId();

        this.courseMembershipHandler.addUserToCourseIfNotAlready(courseEntity, user);
        try {
            CompositeProgressInformation progressInformation = contentServiceClient.queryProgressByChapterId(userId, chapterId);
            if (progressInformation.getCompletedContents() == progressInformation.getTotalContents()) {
                CompletedSpecificChapterGoalProgressEvent completedSpecificChapterGoalProgressEvent = getCompleteSpecificChapterGoalProgressEvent(userId, chapterId, courseId);
                this.goalProgressUpdater.updateGoalProgressEntitiesForUser(user, courseId, completedSpecificChapterGoalProgressEvent);
            }
        } catch (ContentServiceConnectionException e) {
            throw new TransientEventListenerException(e);
        }
    }

    @NotNull
    private static CompletedSpecificChapterGoalProgressEvent getCompleteSpecificChapterGoalProgressEvent(UUID userId, UUID chapterId, UUID courseId) {
        return CompletedSpecificChapterGoalProgressEvent
                .builder()
                .userId(userId)
                .chapterId(chapterId)
                .build();
    }

}
