package de.unistuttgart.iste.meitrex.gamification_service.service.functional;

import org.springframework.stereotype.*;
import org.springframework.beans.factory.annotation.*;


@Component
class DefaultScoringFunction implements IScoringFunction {

    private static final String EXCEPTION_MSG_DECAY_RATE_MUST_BE_NEGATIVE
            = "Non-negative decay rate is invalid.";

    private static final String EXCEPTION_MSG_MULTIPLICATIVE_WEIGHT_MUST_BE_POSITIVE
            = "Negative multiplicative weight is invalid.";

    // Validation logic

    private static double assureIsValidDecayRate(double decayRate) {
        if(decayRate >= 0) {
            throw new IllegalArgumentException(EXCEPTION_MSG_DECAY_RATE_MUST_BE_NEGATIVE);
        }
        return decayRate;
    }

    private static double assureIsValidMultiplicativeWeight(double multiplicativeWeight) {
        if(multiplicativeWeight < 0) {
            throw new IllegalArgumentException(EXCEPTION_MSG_MULTIPLICATIVE_WEIGHT_MUST_BE_POSITIVE);
        }
        return multiplicativeWeight;
    }

    //Attributes

    private final double decayRate;

    private final double multiplicativeWeight;


    public DefaultScoringFunction(
            @Value("${gamification.scoring.decay-rate:-0.75}") float decayRate,
            @Value("${gamification.scoring.multiplicativeWeight:100}") float multiplicativeWeight
    ) {
        this.decayRate = assureIsValidDecayRate(decayRate);
        this.multiplicativeWeight = assureIsValidMultiplicativeWeight(multiplicativeWeight);
    }

    @Override
    public double score(double correctness, int attempt) {
        System.out.println("attempt: " + attempt);
        return Math.round(this.multiplicativeWeight*correctness*(Math.pow(2.0, this.decayRate*attempt)));
    }
}
