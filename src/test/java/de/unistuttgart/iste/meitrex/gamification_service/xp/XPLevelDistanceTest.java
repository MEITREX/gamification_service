package de.unistuttgart.iste.meitrex.gamification_service.xp;


import de.unistuttgart.iste.meitrex.gamification_service.service.functional.DefaultXPImplementation;
import de.unistuttgart.iste.meitrex.gamification_service.service.functional.IXPLevelDistance;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class XPLevelDistanceTest {

    private final IXPLevelDistance defaultXPImplementation = new DefaultXPImplementation(40, 600);

    @Test
    public void testCalcDistanceContract() {
        assertThrows(IllegalArgumentException.class, ()-> defaultXPImplementation.calcDistance(0, 50));
    }

    @Test
    public void testCalcDistance() {
        final Map<Double, Double> fromZeroToLevel20 = Map.of(
                0.0, 8837.482010094142,
                1000.0,  7837.4820100941415,
                2000.0,  6837.4820100941415,
                3000.0, 5837.4820100941415,
                6000.0, 2837.4820100941415,
                9000.0, -162.51798990585849
        );
        for(Map.Entry<Double, Double> curCase : fromZeroToLevel20.entrySet()) {
            assertEquals(curCase.getValue(), this.defaultXPImplementation.calcDistance(curCase.getKey(), 20));
        }
    }

    @Test
    public void testCalcDistanceFromZeroXPToMaxLevel() {
        assertEquals(23283.269035830166, this.defaultXPImplementation.calcDistance(0.0, 40));
    }

}
