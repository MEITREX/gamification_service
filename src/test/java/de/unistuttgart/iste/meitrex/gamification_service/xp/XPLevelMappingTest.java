package de.unistuttgart.iste.meitrex.gamification_service.xp;


import java.util.*;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import de.unistuttgart.iste.meitrex.gamification_service.service.functional.*;

public class XPLevelMappingTest {

    private final IXPLevelMapping defaultXPImplementation = new DefaultXPImplementation(40, 600);

    @Test
    public void testCalcLevelContract() {
        assertThrows(IllegalArgumentException.class, ()-> defaultXPImplementation.calcLevel(-10.0));
        assertDoesNotThrow(()-> defaultXPImplementation.calcLevel(0));
        assertDoesNotThrow(()-> defaultXPImplementation.calcLevel(10));
    }

    @Test
    public void testCalcLevel(){
        final Map<Double, Integer> xpLevelMap = Map.of(
                0.0, 0,
                1000.0, 4,
                    2000.0, 7,
                    3000.0, 9,
                    6000.0, 15,
                    9000.0, 20
                );
        for(Map.Entry<Double, Integer> curCase : xpLevelMap.entrySet()) {
            assertEquals(curCase.getValue(), this.defaultXPImplementation.calcLevel(curCase.getKey()));
        }
    }

}
