package de.unistuttgart.iste.meitrex.gamification_service.service.internal;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.UserEntity;

/**
 * A contract for adding XP to a user's balance. Client code may either specify amount of xp to add explicitly or rely on
 * the predefined event model in the form of {@see Cause}.
 *
 * @author Philipp Kunz
 */
public interface IUserXPAdder {

    enum Cause {
        NEW_FORUM_POST, ANSWER_ACCEPTED, ACHIEVEMENT_COMPLETED
    }

    void add(UserEntity entity, int value);

    void add(UserEntity entity, Cause cause);
}
