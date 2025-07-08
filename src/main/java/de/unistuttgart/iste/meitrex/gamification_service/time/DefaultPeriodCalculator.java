package de.unistuttgart.iste.meitrex.gamification_service.time;


import java.sql.Timestamp;
import java.time.*;
import java.util.Objects;

import org.springframework.stereotype.*;

/**
 *
 * @author Philipp Kunz
 */
@Component
class DefaultPeriodCalculator implements IPeriodCalculator {

    // Error messages

    private static final String ERR_MSG_ALL_TIME_NOT_SUPPORTED
            = "Period type ALL_TIME is not supported.";

    private static final String ERR_MSG_INVALID_START_DATE_FOR_WEEKLY_PERIOD
            = "A weekly period must start on Monday.";

    private static final String ERR_MSG_INVALID_START_DATE_FOR_MONTHLY_PERIOD
            = "A monthly period must start on the first of a month.";


    //Validation logic

    private static void assureIsNonNull(Period period) {
        Objects.requireNonNull(period);
    }

    private static void assureIsNonNegative(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Timestamp must be non-negative.");
        }
    }

    private static void assureIsValidBeginDate(LocalDate beginDate, Period period) {
        if(Period.ALL_TIME.equals(period)) {
            throw new IllegalArgumentException(ERR_MSG_ALL_TIME_NOT_SUPPORTED);
        }
        else if(Period.WEEKLY.equals(period)
                && !DayOfWeek.MONDAY.equals(beginDate.getDayOfWeek())) {
            throw new IllegalArgumentException(ERR_MSG_INVALID_START_DATE_FOR_WEEKLY_PERIOD);
        }
        else if(Period.MONTHLY.equals(period)
                && beginDate.getDayOfMonth() != 1) {
            throw new IllegalArgumentException(ERR_MSG_INVALID_START_DATE_FOR_MONTHLY_PERIOD);
        }
    }


    // Business logic

    private static LocalDate calcWeeklyStartDate(Long timestamp) {
        LocalDate localDate = new Timestamp(timestamp)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        int daysToMonday = localDate.getDayOfWeek().getValue() - 1;
        return localDate.minusDays(daysToMonday);
    }

    private static LocalDate calcMonthlyStartDate(Long timestamp) {
        LocalDate localDate = new Timestamp(timestamp)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        int daysToFirstDayOfMonth = localDate.getDayOfMonth() - 1;
        return localDate.minusDays(daysToFirstDayOfMonth);
    }

    private static LocalDate calcWeeklyEndDate(LocalDate beginDate) {
        return beginDate.plusDays(6);
    }

    private static LocalDate calcMonthlyEndDate(LocalDate beginDate) {
        return LocalDate.of(beginDate.getYear(), beginDate.getMonthValue(), beginDate.lengthOfMonth());
    }



    @Override
    public LocalDate calcStartDate(long timestamp, Period period) {
        assureIsNonNull(period);
        assureIsNonNegative(timestamp);

        return switch (period) {
            case WEEKLY -> calcWeeklyStartDate(timestamp);

            case MONTHLY -> calcMonthlyStartDate(timestamp);

            default -> throw new IllegalArgumentException();
        };
    }


    @Override
    public LocalDate calcSucStartDate(LocalDate startDate, Period period) {
        assureIsValidBeginDate(startDate, period);

        LocalDate endDate = switch (period) {

            case WEEKLY -> calcWeeklyEndDate(startDate);

            case MONTHLY -> calcMonthlyEndDate(startDate);

            default -> throw new IllegalArgumentException(ERR_MSG_ALL_TIME_NOT_SUPPORTED);

        };

        return endDate.plusDays(1);
    }
}



