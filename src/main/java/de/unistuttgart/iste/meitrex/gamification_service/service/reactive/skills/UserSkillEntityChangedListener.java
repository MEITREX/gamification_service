package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.skills;

import de.unistuttgart.iste.meitrex.common.event.CrudOperation;
import de.unistuttgart.iste.meitrex.gamification_service.events.internal.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentSkillEntityChangedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentSkillEntityChangedEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentUserProgressUpdatedRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.LeaderboardEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.skilllevels.SkillEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ILeaderboardRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserScoreRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.SkillRepository;
import de.unistuttgart.iste.meitrex.gamification_service.service.functional.IScoringFunction;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ICourseCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.ISkillCreator;
import de.unistuttgart.iste.meitrex.gamification_service.service.internal.IUserCreator;
import de.unistuttgart.iste.meitrex.gamification_service.time.IPeriodCalculator;
import de.unistuttgart.iste.meitrex.gamification_service.time.ITimeService;
import de.unistuttgart.iste.meitrex.gamification_service.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
class UserSkillEntityChangedListener extends AbstractInternalListener<PersistentSkillEntityChangedEvent, InternalSkillEntityChangedEvent> {

    // Do not change to keep unique UUID even in case of refactoring.
    private static final String name = "UserSkillEntityChangedListener";

    private final SkillRepository skillRepository;

    private final ISkillCreator skillCreator;

    public UserSkillEntityChangedListener(
            @Autowired IPersistentSkillEntityChangedEventRepository persistentEventRepository,
            @Autowired  IPersistentEventStatusRepository eventStatusRepository,
            @Autowired ITimeService timeService,
            @Autowired  SkillRepository skillRepository,
            @Autowired ISkillCreator skillCreator
    ) {
        super(persistentEventRepository, eventStatusRepository, timeService);
        this.skillRepository = Objects.requireNonNull(skillRepository);
        this.skillCreator = Objects.requireNonNull(skillCreator);
    }

    @Override
    @EventListener
    public void process(InternalSkillEntityChangedEvent internalEvent) {
        super.process(internalEvent);
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    protected void doProcess(PersistentSkillEntityChangedEvent persistentEvent)
            throws TransientEventListenerException, NonTransientEventListenerException {
        if(persistentEvent.getOperation() == CrudOperation.DELETE) {
            skillRepository.deleteById(persistentEvent.getSkillId());
            return;
        }
        SkillEntity skillEntity = skillCreator.fetchOrCreate(persistentEvent.getSkillId());
        if(persistentEvent.getSkillName() != null) {
            skillEntity.setName(persistentEvent.getSkillName());
        }
        skillRepository.save(skillEntity);
    }
}
