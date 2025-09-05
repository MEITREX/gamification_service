package de.unistuttgart.iste.meitrex.gamification_service.service.internal;

import java.util.*;

import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.*;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.repository.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.skilllevels.*;

@Component
class DefaultSkillCreator implements ISkillCreator {

    private final SkillRepository skillRepository;

    public DefaultSkillCreator(@Autowired SkillRepository skillRepository) {
         this.skillRepository = Objects.requireNonNull(skillRepository);
    }

    public SkillEntity fetchOrCreate(final UUID skillId) {
        return skillRepository.findById(skillId)
                .orElseGet(() -> skillRepository.save(new SkillEntity(skillId, null)));
    }
}
