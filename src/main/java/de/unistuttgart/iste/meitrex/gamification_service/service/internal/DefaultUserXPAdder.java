package de.unistuttgart.iste.meitrex.gamification_service.service.internal;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;
import de.unistuttgart.iste.meitrex.generated.dto.UserItem;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class DefaultUserXPAdder implements IUserXPAdder{

    private static void assureNonNull(int value) {
        if(value < 0) {
            throw new IllegalArgumentException();
        }
    }


    private final Map<Cause, Integer> causeXPMap = new HashMap<>();

    public DefaultUserXPAdder() {
        causeXPMap.put(Cause.NEW_FORUM_POST, 20);
        causeXPMap.put(Cause.ACHIEVEMENT_COMPLETED, 30);
        causeXPMap.put(Cause.ANSWER_ACCEPTED, 80);
        causeXPMap.put(Cause.STAGE_COMPLETED, 20);
        causeXPMap.put(Cause.CHAPTER_COMPLETED, 200);
        causeXPMap.put(Cause.COURSE_COMPLETED, 500);
        causeXPMap.put(Cause.ASSIGNMENT_COMPLETED, 80);
        causeXPMap.put(Cause.QUIZ_COMPLETED, 2);
        causeXPMap.put(Cause.FLASHCARD_COMPLETED, 2);
        causeXPMap.put(Cause.VIDEO_WATCHED, 2);
        causeXPMap.put(Cause.DOCUMENT_OPENED, 2);
    }

    @Override
    public void add(UserEntity entity, int value) {
        Objects.requireNonNull(entity);
        assureNonNull(value);
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

    @Override
    public void add(UserEntity entity, Cause cause, int multiple) {
        assureNonNull(multiple);
        this.add(entity, causeXPMap.get(Objects.requireNonNull(cause)) * multiple);
    }
}
