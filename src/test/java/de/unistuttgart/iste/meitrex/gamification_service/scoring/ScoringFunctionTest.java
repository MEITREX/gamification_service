package de.unistuttgart.iste.meitrex.gamification_service.scoring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.unistuttgart.iste.meitrex.gamification_service.service.functional.DefaultScoringFunction;

public class ScoringFunctionTest {

    private final DefaultScoringFunction scoringFunction = new DefaultScoringFunction(-0.75F, 100.0F);

    @Test
    public void testScoreAsCorrectnessChanges() {
        assertEquals(0, this.scoringFunction.score(0.0, 1));
        assertEquals(12, this.scoringFunction.score(.2,1));
        assertEquals(24, this.scoringFunction.score(.4, 1));
        assertEquals(36, this.scoringFunction.score(.6, 1));
        assertEquals(48, this.scoringFunction.score(.8, 1));
        assertEquals(59, this.scoringFunction.score(1.0, 1));
    }

    @Test
    public void testScoreAsAttemptChanges() {
        assertEquals(0, this.scoringFunction.score(0.0, 1));
        assertEquals(0, this.scoringFunction.score(0.0, 2));
        assertEquals(0, this.scoringFunction.score(0.0, 3));
        assertEquals(297, this.scoringFunction.score(5.0, 1));
        assertEquals(177, this.scoringFunction.score(5.0, 2));
        assertEquals(105, this.scoringFunction.score(5.0, 3));
    }
}
