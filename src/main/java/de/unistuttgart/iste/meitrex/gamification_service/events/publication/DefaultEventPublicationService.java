package de.unistuttgart.iste.meitrex.gamification_service.events.publication;

import de.unistuttgart.iste.meitrex.gamification_service.aspects.logging.Loggable;
import de.unistuttgart.iste.meitrex.gamification_service.events.PersistentMediaRecordWorkedOnEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

/**
 * Default implementation of {@link IEventPublicationService} that persists incoming persistent events,
 * ensures that duplicate events are not processed, and publishes corresponding internal events
 * after a successful transaction commit.
 *
 * This service uses a type-to-handler map to delegate the persistence and conversion logic
 * for different event types. The {@link org.springframework.transaction.support.TransactionSynchronizationManager}
 * is used to ensure that publication happens only after the surrounding database transaction commits successfully.
 *
 * Duplicate detection is applied only to events that implement the {@link de.unistuttgart.iste.meitrex.gamification_service.events.persistent.ISequenced}
 * interface. For these events, the message sequence number is checked against the database to prevent reprocessing
 * of duplicates. All other events are always treated as new and published unconditionally.
 *
 * To support additional event types, implement a corresponding persistence and conversion method (e.g. {@code saveMyEventType}),
 * and register it in the {@code handlerMap} inside the constructor, mapping the new persistent event class
 * to the handler function. This allows the service to process and publish the new event type without modifying
 * the core publishing logic.
 *
 * @author Philipp Kunz
 */

@Slf4j
@Component
class DefaultEventPublicationService implements IEventPublicationService {

    private static final String ERR_MSG_UNSUPPORTED_EVENT_TYPE = "The passed persistent event is not supported.";

    private static final String ERR_MSG_NO_SEQ_NO = "A persist event must feature a valid sequence no.";

    private static final String ERR_MSG_DUPLICATE_RECEIVED = "Ignored message {} since the passed sequence no  {} has already been passed";


    private final TransactionalApplicationEventPublisher applicationEventPublisher;

    private final IPersistentEventRepository persistentEventRepository;

    private final IPersistentUserProgressUpdatedRepository userProgressUpdatedRepository;

    private final IPersistentContentProgressedRepository contentProgressedRepository;

    private final IPersistentForumActivityRepository forumActivityRepository;

    private final IPersistentSkillEntityChangedEventRepository skillEntityChangedEventRepository;

    private final IPersistentUserSkillLevelChangedEventRepository userSkillLevelChangedEventRepository;

    private final IPersistentMediaRecordInfoRepository mediaRecordInfoRepository;

    private final IPersistentAskedTutorAQuestionEventRepository askedTutorAQuestionEventRepository;

    private final IPersistentChapterCompletedEventRepository chapterCompletedEventRepository;

    private final IPersistentStageCompletedEventRepository stageCompletedEventRepository;

    private final IPersistentCourseCompletedEventRepository courseCompletedEventRepository;

    private final IPersistentUserCourseMembershipChangedEventRepository userCourseMembershipChangedEventRepository;

    private final IPersistentMediaRecordWorkedOnRepository persistentMediaRecordWorkedOnRepository;

    private final IPersistentSubmissionCompletedEventRepository persistentSubmissionCompletedEventRepository;

    private final Map<Class<? extends PersistentEvent>, Function<PersistentEvent, InternalEvent>> handlerMap = new HashMap<>();


    public DefaultEventPublicationService(
            @Autowired TransactionalApplicationEventPublisher applicationEventPublisher,
            @Autowired @Qualifier("default") IPersistentEventRepository persistentEventRepository,
            @Autowired IPersistentUserProgressUpdatedRepository userProgressUpdatedRepository,
            @Autowired IPersistentContentProgressedRepository contentProgressedRepository,
            @Autowired IPersistentForumActivityRepository forumActivityRepository,
            @Autowired IPersistentSkillEntityChangedEventRepository skillEntityChangedEventRepository,
            @Autowired IPersistentUserSkillLevelChangedEventRepository userSkillLevelChangedEventRepository,
            @Autowired IPersistentMediaRecordInfoRepository mediaRecordInfoRepository,
            @Autowired IPersistentAskedTutorAQuestionEventRepository askedTutorAQuestionEventRepository,
            @Autowired IPersistentChapterCompletedEventRepository chapterCompletedEventRepository,
            @Autowired IPersistentStageCompletedEventRepository stageCompletedEventRepository,
            @Autowired IPersistentCourseCompletedEventRepository courseCompletedEventRepository,
            @Autowired IPersistentUserCourseMembershipChangedEventRepository userCourseMembershipChangedEventRepository,
            @Autowired IPersistentMediaRecordWorkedOnRepository persistentMediaRecordWorkedOnRepository,
            @Autowired IPersistentSubmissionCompletedEventRepository persistentSubmissionCompletedEventRepository
    ) {
        this.applicationEventPublisher = Objects.requireNonNull(applicationEventPublisher);
        this.persistentEventRepository = Objects.requireNonNull(persistentEventRepository);
        this.userProgressUpdatedRepository = Objects.requireNonNull(userProgressUpdatedRepository);
        this.contentProgressedRepository = Objects.requireNonNull(contentProgressedRepository);
        this.forumActivityRepository = Objects.requireNonNull(forumActivityRepository);
        this.skillEntityChangedEventRepository = Objects.requireNonNull(skillEntityChangedEventRepository);
        this.userSkillLevelChangedEventRepository = Objects.requireNonNull(userSkillLevelChangedEventRepository);
        this.mediaRecordInfoRepository = Objects.requireNonNull(mediaRecordInfoRepository);
        this.askedTutorAQuestionEventRepository = Objects.requireNonNull(askedTutorAQuestionEventRepository);
        this.chapterCompletedEventRepository = Objects.requireNonNull(chapterCompletedEventRepository);
        this.stageCompletedEventRepository = Objects.requireNonNull(stageCompletedEventRepository);
        this.courseCompletedEventRepository = Objects.requireNonNull(courseCompletedEventRepository);
        this.userCourseMembershipChangedEventRepository = Objects.requireNonNull(userCourseMembershipChangedEventRepository);
        this.persistentMediaRecordWorkedOnRepository = Objects.requireNonNull(persistentMediaRecordWorkedOnRepository);
        this.persistentSubmissionCompletedEventRepository = Objects.requireNonNull(persistentSubmissionCompletedEventRepository);
        this.handlerMap.put(PersistentUserProgressUpdatedEvent.class, this::saveUserProgressUpdatedEvent);
        this.handlerMap.put(PersistentContentProgressedEvent.class, this::saveContentProgressedEvent);
        this.handlerMap.put(PersistentForumActivityEvent.class, this::saveForumActivityEvent);
        this.handlerMap.put(PersistentSkillEntityChangedEvent.class, this::saveSkillEntityChangedEvent);
        this.handlerMap.put(PersistentUserSkillLevelChangedEvent.class, this::saveUserSkillLevelChangedEvent);
        this.handlerMap.put(PersistentMediaRecordInfoEvent.class, this::saveMediaRecordInfoEvent);
        this.handlerMap.put(PersistentMediaRecordWorkedOnEvent.class, this::saveMediaRecordWorkedOnEvent);
        this.handlerMap.put(PersistentChapterCompletedEvent.class, this::saveChapterCompletedEvent);
        this.handlerMap.put(PersistentStageCompletedEvent.class, this::saveStageCompletedEvent);
        this.handlerMap.put(PersistentCourseCompletedEvent.class, this::saveCourseCompletedEvent);
        this.handlerMap.put(PersistentAskedTutorAQuestionEvent.class, this::saveAskedTutorAQuestionEvent);
        this.handlerMap.put(PersistentUserCourseMembershipChangedEvent.class, this::saveUserCourseMembershipChangedEvent);
        this.handlerMap.put(PersistentSubmissionCompletedEvent.class, this::saveSubmissionCompletedEvent);
    }

    @Override
    @Loggable(
            inLogLevel = Loggable.LogLevel.DEBUG,
            exitLogLevel = Loggable.LogLevel.DEBUG,
            exceptionLogLevel = Loggable.LogLevel.WARN,
            logArgs = false,
            logExecutionTime = false
    )
    public void saveCommitAndPublishIfNew(PersistentEvent persistentEvent) {
        if(!this.handlerMap.containsKey(persistentEvent.getClass())) {
            throw new IllegalArgumentException(ERR_MSG_UNSUPPORTED_EVENT_TYPE);
        }
        if(isNew(persistentEvent)) {
            final InternalEvent internalEvent = this.handlerMap.get(persistentEvent.getClass())
                    .apply(persistentEvent);
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    applicationEventPublisher.publishEvent(internalEvent);
                }
            });
        }
        else {
            log.info(ERR_MSG_DUPLICATE_RECEIVED, persistentEvent, persistentEvent.getMsgSequenceNo());
        }
    }

    private boolean isNew(PersistentEvent persistentEvent) {

        if(!(persistentEvent instanceof ISequenced)) {
            return true;
        }
        final Long seqNo = persistentEvent.getMsgSequenceNo();
        if(Objects.isNull(seqNo)) {
            throw new IllegalArgumentException(ERR_MSG_NO_SEQ_NO);
        }
        return this.persistentEventRepository.
                findByMsgSequenceNo(seqNo)
                .isEmpty();

    }

    private InternalEvent saveUserProgressUpdatedEvent(PersistentEvent persistentEvent) {

        PersistentUserProgressUpdatedEvent persistentUserProgressEvent = (PersistentUserProgressUpdatedEvent) persistentEvent;

        final UUID uuid = this.userProgressUpdatedRepository.save(persistentUserProgressEvent)
                .getUuid();

        return new InternalUserProgressUpdatedEvent(DefaultEventPublicationService.this, uuid);
    }

    private InternalEvent saveContentProgressedEvent(PersistentEvent persistentEvent) {

        PersistentContentProgressedEvent persistentContentProgressedEvent = (PersistentContentProgressedEvent) persistentEvent;

        final UUID uuid = this.contentProgressedRepository.save(persistentContentProgressedEvent)
                .getUuid();

        return new InternalContentProgressedEvent(DefaultEventPublicationService.this, uuid);
    }

    private InternalEvent saveForumActivityEvent(PersistentEvent persistentEvent) {
        PersistentForumActivityEvent persistentForumActivityEvent = (PersistentForumActivityEvent) persistentEvent;

        final UUID uuid = this.forumActivityRepository.save(persistentForumActivityEvent)
                .getUuid();

        return new InternalForumActivityEvent(DefaultEventPublicationService.this, uuid);
    }

    private InternalEvent saveSkillEntityChangedEvent(PersistentEvent persistentEvent) {
        PersistentSkillEntityChangedEvent persistentForumActivityEvent = (PersistentSkillEntityChangedEvent) persistentEvent;

        final UUID uuid = this.skillEntityChangedEventRepository.save(persistentForumActivityEvent)
                .getUuid();

        return new InternalSkillEntityChangedEvent(DefaultEventPublicationService.this, uuid);
    }

    private InternalEvent saveUserSkillLevelChangedEvent(PersistentEvent persistentEvent) {
        PersistentUserSkillLevelChangedEvent persistentForumActivityEvent = (PersistentUserSkillLevelChangedEvent) persistentEvent;

        final UUID uuid = this.userSkillLevelChangedEventRepository.save(persistentForumActivityEvent)
                .getUuid();

        return new InternalUserSkillLevelChangedEvent(DefaultEventPublicationService.this, uuid);
    }

    private InternalEvent saveMediaRecordInfoEvent(PersistentEvent persistentEvent) {
        PersistentMediaRecordInfoEvent persistentMediaRecordInfoEvent = (PersistentMediaRecordInfoEvent) persistentEvent;

        final UUID uuid = this.mediaRecordInfoRepository.save(persistentMediaRecordInfoEvent)
                .getUuid();

        return new InternalMediaRecordInfoEvent(DefaultEventPublicationService.this, uuid);
    }

    private InternalEvent saveAskedTutorAQuestionEvent(PersistentEvent persistentEvent) {
        PersistentAskedTutorAQuestionEvent persistentAskedTutorAQuestionEvent
                = (PersistentAskedTutorAQuestionEvent) persistentEvent;

        final UUID uuid = this.askedTutorAQuestionEventRepository.save(persistentAskedTutorAQuestionEvent)
                .getUuid();

        return new InternalAskedTutorAQuestionEvent(DefaultEventPublicationService.this, uuid);
    }

    private InternalEvent saveChapterCompletedEvent(PersistentEvent persistentEvent) {
        PersistentChapterCompletedEvent persistentChapterCompletedEvent =
                (PersistentChapterCompletedEvent) persistentEvent;

        final UUID uuid = this.chapterCompletedEventRepository.save(persistentChapterCompletedEvent)
                .getUuid();

        return new InternalChapterCompletedEvent(DefaultEventPublicationService.this, uuid);
    }

    private InternalEvent saveStageCompletedEvent(PersistentEvent persistentEvent) {
        PersistentStageCompletedEvent persistentStageCompletedEvent = (PersistentStageCompletedEvent) persistentEvent;

        final UUID uuid = this.stageCompletedEventRepository.save(persistentStageCompletedEvent)
                .getUuid();

        return new InternalStageCompletedEvent(DefaultEventPublicationService.this, uuid);
    }

    private InternalEvent saveCourseCompletedEvent(PersistentEvent persistentEvent) {
        PersistentCourseCompletedEvent persistentCourseCompletedEvent =
                (PersistentCourseCompletedEvent) persistentEvent;

        final UUID uuid = this.courseCompletedEventRepository.save(persistentCourseCompletedEvent)
                .getUuid();

        return new InternalCourseCompletedEvent(DefaultEventPublicationService.this, uuid);
    }

    private InternalEvent saveMediaRecordWorkedOnEvent(PersistentEvent persistentEvent) {
        PersistentMediaRecordWorkedOnEvent persistentMediaRecordWorkedOnEvent
                = (PersistentMediaRecordWorkedOnEvent) persistentEvent;


        final UUID uuid = this.persistentMediaRecordWorkedOnRepository
                .save(persistentMediaRecordWorkedOnEvent)
                .getUuid();


        return new InternalMediaWorkedOnEvent(DefaultEventPublicationService.this, uuid);
    }

    private InternalEvent saveUserCourseMembershipChangedEvent(PersistentEvent persistentEvent) {
        PersistentUserCourseMembershipChangedEvent persistentUserCourseMembershipChangedEvent
                = (PersistentUserCourseMembershipChangedEvent) persistentEvent;

        final UUID uuid = this.userCourseMembershipChangedEventRepository
                .save(persistentUserCourseMembershipChangedEvent)
                .getUuid();

        return new InternalUserCourseMembershipChangedEvent(DefaultEventPublicationService.this, uuid);
    }

    private InternalEvent saveSubmissionCompletedEvent(PersistentEvent persistentEvent) {
        PersistentSubmissionCompletedEvent persistentSubmissionCompletedEvent
                = (PersistentSubmissionCompletedEvent) persistentEvent;

        final UUID uuid = this.persistentSubmissionCompletedEventRepository
                .save(persistentSubmissionCompletedEvent)
                .getUuid();

        return new InternalSubmissionCompletedEvent(DefaultEventPublicationService.this, uuid);
    }
}
