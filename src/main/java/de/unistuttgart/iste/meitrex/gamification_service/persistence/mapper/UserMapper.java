package de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper;


import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.*;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class UserMapper {

    public User toDTO(UserEntity userEntity, int maxDepth) {
        if(userEntity == null || maxDepth < 0) {
            return null;
        }
        final User user = new User();
        user.setId(userEntity.getId());
        final Double requiredXP = userEntity.getRequiredXP();
        final Double exceedingXP = userEntity.getExceedingXP();
        final Integer level = userEntity.getLevel();
        if(Objects.nonNull(requiredXP)) {
            userEntity.setRequiredXP(requiredXP);
        }
        if(Objects.nonNull(exceedingXP)) {
            userEntity.setExceedingXP(exceedingXP);
        }
        if(Objects.nonNull(level)) {
            userEntity.setLevel(level);
        }
        user.setName(userEntity.getUserName());
        return user;
    }

}
