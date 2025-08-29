package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.achievements;

import de.unistuttgart.iste.meitrex.content_service.client.ContentServiceClient;
import de.unistuttgart.iste.meitrex.content_service.exception.ContentServiceConnectionException;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.CompletedQuizzesGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.achievements.goalProgressEvents.CompletedSpecificContentGoalProgressEvent;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseMembershipHandler;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.achievements.IGoalProgressUpdater;
import de.unistuttgart.iste.meitrex.generated.dto.Content;
import de.unistuttgart.iste.meitrex.generated.dto.ContentType;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.*;

import de.unistuttgart.iste.meitrex.gamification_service.time.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.*;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
class ContentProgressedEventAchievementListener extends AbstractInternalListener<PersistentContentProgressedEvent, InternalContentProgressedEvent>   {

    // Do not change to keep unique UUID even in case of refactoring.
    private static final String name = "ContentProgressedAchievementListener";

    private ICourseCreator courseCreator;

    private IUserCreator userCreator;

    private ICourseMembershipHandler courseMembershipHandler;

    private ContentServiceClient contentServiceClient;

    private IGoalProgressUpdater goalProgressUpdater;

    public ContentProgressedEventAchievementListener(
            @Autowired IPersistentContentProgressedRepository persistentEventRepository,
            @Autowired  IPersistentEventStatusRepository eventStatusRepository,
            @Autowired  ITimeService timeService,
            @Autowired  ICourseCreator courseCreator,
            @Autowired IUserCreator userCreator,
            @Autowired ICourseMembershipHandler courseMembershipHandler,
            @Autowired ContentServiceClient contentServiceClient,
            @Autowired IGoalProgressUpdater goalProgressUpdater
    ) {
        super(persistentEventRepository, eventStatusRepository, timeService);
        this.courseCreator = Objects.requireNonNull(courseCreator);
        this.userCreator = Objects.requireNonNull(userCreator);
        this.courseMembershipHandler = Objects.requireNonNull(courseMembershipHandler);
        this.contentServiceClient = Objects.requireNonNull(contentServiceClient);
        this.goalProgressUpdater = Objects.requireNonNull(goalProgressUpdater);
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
    protected void doProcess(PersistentContentProgressedEvent persistentEvent)
            throws TransientEventListenerException, NonTransientEventListenerException {
        UUID userId = persistentEvent.getUserId();
        Content content;
        try {
            content = contentServiceClient
                    .queryContentsByIds(userId, List.of(persistentEvent.getContentId()))
                    .getFirst();

        } catch (ContentServiceConnectionException e) {
            throw new TransientEventListenerException(e);
        }
        final UUID courseId = content.getMetadata().getCourseId();
        final CourseEntity courseEntity = courseCreator.fetchOrCreate(courseId);
        final UserEntity user = userCreator.fetchOrCreate(userId);

        this.courseMembershipHandler.addUserToCourseIfNotAlready(courseEntity, user);

        if (Objects.requireNonNull(content.getMetadata().getType()) == ContentType.QUIZ) {
            quizProgress(persistentEvent, user, courseId);
        }

        final CompletedSpecificContentGoalProgressEvent gpe = CompletedSpecificContentGoalProgressEvent.builder()
                .userId(userId)
                .contentId(content.getId())
                .build();

        this.goalProgressUpdater.updateGoalProgressEntitiesForUser(user, courseId, gpe);
    }


    private void quizProgress(final PersistentContentProgressedEvent persistentEvent, UserEntity user, UUID courseId) {
        CompletedQuizzesGoalProgressEvent completedQuizzesGoalProgressEvent = getCompletedQuizzesGoalProgressEvent(persistentEvent, user);
        this.goalProgressUpdater.updateGoalProgressEntitiesForUser(user, courseId, completedQuizzesGoalProgressEvent);
    }


    @NotNull
    private static CompletedQuizzesGoalProgressEvent getCompletedQuizzesGoalProgressEvent(PersistentContentProgressedEvent persistentEvent, UserEntity user) {
        UUID contendId = persistentEvent.getContentId();
        float correctness = (float) persistentEvent.getCorrectness();
        return CompletedQuizzesGoalProgressEvent.builder()
                .userId(user.getId())
                .score(correctness)
                .contentId(contendId)
                .build();
    }

}
