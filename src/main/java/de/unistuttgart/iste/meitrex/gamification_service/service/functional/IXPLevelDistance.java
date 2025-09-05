package de.unistuttgart.iste.meitrex.gamification_service.service.functional;


/**
 * Functional interface for computing the XP (experience points) distance required to reach a specified target level
 * from a given current XP value.
 *
 * @author Philipp Kunz
 */
@FunctionalInterface
public interface IXPLevelDistance {

    /**
     * Calculates the remaining XP required to reach the specified target level, based on the current XP value.
     *
     * @param curXP the user's current XP value (must be non-negative)
     * @param targetLevel the level the user aims to reach (must be supported by the implementation)
     * @throws IllegalArgumentException if {@code curXP} is negative or if the {@code targetLevel} is unsupported
     * @return the amount of XP needed to reach the target level, if {@code curXP} already exceeds the threshold,
     * a negative value may be returned
     */
    double calcDistance(double curXP, int targetLevel);

}
