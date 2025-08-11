package de.unistuttgart.iste.meitrex.gamification_service.service.scoring;

/**
 * A scoring function maps a correctness value achieved for an assessment onto a double score reflecting the user's
 * performance. To account for adaptation and practice, the number of attempts is factored in. However, the actual
 * implications of repetitive participation are treated as an implementation-specific detail.
 *
 * @author Philipp Kunz
 *
 */
@FunctionalInterface
public interface IScoringFunction {

    /**
     * Maps the passed correctness and the corresponding attempt count onto a double value reflecting
     * the user's overall performance.
     *
     * @param correctness a double value reflecting the user’s performance in their last attempt.
     * @param attempt the current attempt.
     * @return a double value reflecting the user’s overall performance.
     */
    double score(double correctness, int attempt);
}
