package de.unistuttgart.iste.meitrex.gamification_service.service.internal;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.generated.dto.UserItem;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
class DefaultUserXPAdder implements IUserXPAdder{

    private final Map<Cause, Integer> causeXPMap = new HashMap<>();

    public DefaultUserXPAdder() {
        causeXPMap.put(Cause.NEW_FORUM_POST, 20);
        causeXPMap.put(Cause.ACHIEVEMENT_COMPLETED, 30);
        causeXPMap.put(Cause.ANSWER_ACCEPTED, 80);
    }

    @Override
    public void add(UserEntity entity, int value) {
        Objects.requireNonNull(entity);
        Integer curXPValue = entity.getXpValue();
        if(entity.getXpValue() == null) {
            curXPValue = 0;
        }
        final Integer newXPValue = curXPValue + value;
        entity.setXpValue(newXPValue);
    }

    @Override
    public void add(UserEntity entity, Cause cause) {
        this.add(entity, causeXPMap.get(Objects.requireNonNull(cause)));
    }
}
