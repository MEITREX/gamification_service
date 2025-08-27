package de.unistuttgart.iste.meitrex.gamification_service.service.reactive.skills;

import de.unistuttgart.iste.meitrex.gamification_service.events.internal.*;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentSkillEntityChangedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserProgressUpdatedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.persistent.PersistentUserSkillLevelChangedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentEventStatusRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentSkillEntityChangedEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentUserProgressUpdatedRepository;
import de.unistuttgart.iste.meitrex.gamification_service.events.repository.IPersistentUserSkillLevelChangedEventRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.LeaderboardEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserScoreEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.skilllevels.SkillEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.skilllevels.SkillLevelsEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.ILeaderboardRepository;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.IUserScoreRepository;
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
class UserSkillLevelChangedListener extends AbstractInternalListener<PersistentUserSkillLevelChangedEvent, InternalUserSkillLevelChangedEvent> {

    // Do not change to keep unique UUID even in case of refactoring.
    private static final String name = "UserSkillLevelChangedListener";

    private final IUserCreator userCreator;

    private final ISkillCreator skillCreator;

    public UserSkillLevelChangedListener(@Autowired IPersistentUserSkillLevelChangedEventRepository persistentEventRepository, @Autowired  IPersistentEventStatusRepository eventStatusRepository, @Autowired ITimeService timeService, @Autowired IUserCreator userCreator, @Autowired ISkillCreator skillCreator) {
        super(persistentEventRepository, eventStatusRepository, timeService);
        this.userCreator = Objects.requireNonNull(userCreator);
        this.skillCreator = Objects.requireNonNull(skillCreator);
    }

    @Override
    @EventListener
    public void process(InternalUserSkillLevelChangedEvent internalEvent) {
        super.process(internalEvent);
    }

    @Override
    protected String getName() {
        return name;
    }

    @Override
    protected void doProcess(PersistentUserSkillLevelChangedEvent persistentEvent)
            throws TransientEventListenerException, NonTransientEventListenerException {
        UserEntity userEntity = userCreator.fetchOrCreate(persistentEvent.getUserId());
        Optional<SkillLevelsEntity> skillLevels = userEntity.getSkillLevelsForSkill(persistentEvent.getSkillId());
        if(skillLevels.isEmpty()) {
            SkillEntity skillEntity = skillCreator.fetchOrCreate(persistentEvent.getSkillId());
            skillLevels = Optional.of(new SkillLevelsEntity(skillEntity, userEntity));
            userEntity.getSkillLevels().add(skillLevels.get());
        }
        skillLevels.get().setBloomLevelValue(persistentEvent.getBloomLevel(), persistentEvent.getNewValue());
    }
}


