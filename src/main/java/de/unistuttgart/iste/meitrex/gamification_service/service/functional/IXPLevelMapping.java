package de.unistuttgart.iste.meitrex.gamification_service.service.functional;

/**
 * Functional interface specifying a contract for mapping xp values onto the corresponding user level represented
 * by an integer. The concrete mapping is implementation-specific.
 *
 * @author Philipp Kunz
 */
@FunctionalInterface
public interface IXPLevelMapping {

    /**
     * Accepts a user's current xp value and returns the corresponding level.
     *
     * @param xpValue a user's current xp value to be mapped onto the corresponding level.
     * @throws IllegalArgumentException if {@code xpValue} is negative
     * @return the user's level corresponding to the passed xpValue.
     */
    int calcLevel(double xpValue);
}
