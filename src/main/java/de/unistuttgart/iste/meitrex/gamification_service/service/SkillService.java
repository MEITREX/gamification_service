package de.unistuttgart.iste.meitrex.gamification_service.service;

import de.unistuttgart.iste.meitrex.common.event.CrudOperation;
import de.unistuttgart.iste.meitrex.common.event.skilllevels.SkillEntityChangedEvent;
import de.unistuttgart.iste.meitrex.common.event.skilllevels.UserSkillLevelChangedEvent;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.skilllevels.SkillEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.skilllevels.SkillLevelsEntity;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.SkillRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class SkillService {
    private final UserService userService;
    private final SkillRepository skillRepository;

    public SkillEntity getOrCreateSkill(final UUID skillId) {
        return skillRepository.findById(skillId)
                .orElseGet(() -> createSkill(skillId));
    }

    public void updateSkillFromEvent(final SkillEntityChangedEvent event) {
        if(event.getOperation() == CrudOperation.DELETE) {
            skillRepository.deleteById(event.getSkillId());
            log.info("Deleted skill with ID: {}", event.getSkillId());
            return;
        }

        SkillEntity skillEntity = skillRepository.findById(event.getSkillId())
                .orElseGet(() -> new SkillEntity(event.getSkillId(), null));

        if(event.getSkillName() != null) {
            skillEntity.setName(event.getSkillName());
        }

        skillEntity = skillRepository.save(skillEntity);
        log.info("Upserted skill from event: {}", skillEntity);
    }

    public SkillEntity createSkill(final UUID skillId) {
        SkillEntity skillEntity = new SkillEntity(skillId, null);
        skillEntity = skillRepository.save(skillEntity);
        log.info("Created skill {}", skillEntity);
        return skillEntity;
    }

    public void updateUserSkillLevelsFromEvent(final UserSkillLevelChangedEvent event) {
        UserEntity userEntity = userService.getOrCreateUser(event.getUserId());
        Optional<SkillLevelsEntity> skillLevels = userEntity.getSkillLevelsForSkill(event.getSkillId());

        if(skillLevels.isEmpty()) {
            SkillEntity skillEntity = getOrCreateSkill(event.getSkillId());
            skillLevels = Optional.of(new SkillLevelsEntity(skillEntity, userEntity));
            userEntity.getSkillLevels().add(skillLevels.get());
            log.info("Created new skill levels for user {} and skill {}", event.getUserId(), event.getSkillId());
        }

        skillLevels.get().setBloomLevelValue(event.getBloomLevel(), event.getNewValue());
        userService.upsertUser(userEntity);
    }
}
