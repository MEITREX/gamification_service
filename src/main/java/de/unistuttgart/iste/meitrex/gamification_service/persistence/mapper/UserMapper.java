package de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper;


import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.*;

import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserMapper {

    public User toDTO(UserEntity userEntity, int maxDepth) {
        if(userEntity == null || maxDepth < 0) {
            return null;
        }
        final User user = new User();
        //TODO Id should store the actual user id.
        user.setId(UUID.randomUUID());
        user.setRefUserID(userEntity.getId());
        final Integer xpValue = userEntity.getXpValue();
        final Double requiredXP = userEntity.getRequiredXP();
        final Double exceedingXP = userEntity.getExceedingXP();
        final Integer level = userEntity.getLevel();
        if(Objects.nonNull(requiredXP)) {
            user.setRequiredXP(requiredXP);
        }
        if(Objects.nonNull(exceedingXP)) {
            user.setExceedingXP(exceedingXP);
        }
        if(Objects.nonNull(level)) {
            user.setLevel(level);
        }
        if(Objects.nonNull(xpValue)) {
            user.setXpValue(xpValue);
        }
        user.setName(userEntity.getUserName());
        return user;
    }

}
