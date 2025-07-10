package de.unistuttgart.iste.meitrex.gamification_service.persistence.mapper;


import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.*;

@Component
@RequiredArgsConstructor
public class UserMapper {

    public User toDTO(UserEntity userEntity, int maxDepth) {
        if(userEntity == null || maxDepth < 0) {
            return null;
        }
        final User user = new User();
        user.setId(userEntity.getId());
        return user;
    }

}
