package de.unistuttgart.iste.meitrex.gamification_service.service.scoring;

/**
 * A scoring function maps a correctness value achieved for an assessment onto an integer score reflecting the user's
 * performance. To account for adaption and practice, the number of attempts is factored in, however, the actual
 * implications of repetitive participation shall be treated as implementation specific detail.
 *
 * @author Philipp Kunz
 *
 */
@FunctionalInterface
public interface IScoringFunction {
    double score(double correctness, int attempt);
}
