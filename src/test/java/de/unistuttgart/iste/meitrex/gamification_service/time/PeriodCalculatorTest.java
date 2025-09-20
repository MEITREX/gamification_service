package de.unistuttgart.iste.meitrex.gamification_service.time;

import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

class PeriodCalculatorTest {

    //14th of September 2025/CEST
    private static final long TODAY = 1757884694995L;

    private final IPeriodCalculator periodCalculator = new DefaultPeriodCalculator(new DefaultTimeService());

    @Test
    void testCheckCalcStartDateContract() {
        assertThrows(NullPointerException.class, () -> periodCalculator.calcStartDate(0, null));
        assertThrows(IllegalArgumentException.class, () -> periodCalculator.calcStartDate(-1, Period.WEEKLY));
        assertThrows(IllegalArgumentException.class, () -> periodCalculator.calcStartDate(0, Period.ALL_TIME));
        assertDoesNotThrow(() -> periodCalculator.calcStartDate(0, Period.WEEKLY));
    }

    @Test
    void testCalcWeeklyStartDate() {
        final LocalDate startDate = periodCalculator.calcStartDate(TODAY, Period.WEEKLY);
        assertEquals(startDate, LocalDate.of(2025, Month.SEPTEMBER, 8));
    }

    @Test
    void testCalcMonthlyStartDate() {
        final LocalDate startDate = periodCalculator.calcStartDate(TODAY, Period.MONTHLY);
        assertEquals(startDate, LocalDate.of(2025, Month.SEPTEMBER, 1));
    }

    @Test
    void testCalcSucWeeklyStartDateContract() {
        assertThrows(IllegalArgumentException.class, () -> {
            periodCalculator.calcSucStartDate( Instant.ofEpochMilli(TODAY)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate(), Period.WEEKLY);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            periodCalculator.calcSucStartDate( Instant.ofEpochMilli(TODAY)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate(), Period.MONTHLY);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            final LocalDate startDate = periodCalculator.calcStartDate(TODAY, Period.WEEKLY);
            periodCalculator.calcSucStartDate(startDate, Period.MONTHLY);
        });
    }

    @Test
    void testCalcSucWeeklyStartDate() {
        final LocalDate startDate = periodCalculator.calcStartDate(TODAY, Period.WEEKLY);
        final LocalDate sucStartDate = periodCalculator.calcSucStartDate(startDate, Period.WEEKLY);
        assertEquals(sucStartDate, LocalDate.of(2025, Month.SEPTEMBER, 15));
    }

    @Test
    void testCalcSucMonthlyStartDate() {
        final LocalDate startDate = periodCalculator.calcStartDate(TODAY, Period.MONTHLY);
        final LocalDate sucStartDate = periodCalculator.calcSucStartDate(startDate, Period.MONTHLY);
        assertEquals(sucStartDate, LocalDate.of(2025, Month.OCTOBER, 1));
    }

}
