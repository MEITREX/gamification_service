package de.unistuttgart.iste.meitrex.gamification_service.time;

import java.time.LocalDate;

/**
 * Allows for querying the current UTC time and local date. An implementation must abstract away from the
 * details of time synchronization and local time zones.
 *
 * @author Philpp Kunz
 */
public interface ITimeService {

    /**
     * Returns the current time in the form of a UTC time stamp measuring the milliseconds passed since the
     * first of January 1970.
     *
     * @return the milliseconds passed since first of January 1970.
     */
    long curTime();


    /**
     * Converts the current time - as returned by curTime - to an instance of {@link LocalDate}. The time zone
     * applied must be treated as an implementation detail.
     *
     * @return given some implementation-specific time-zone, an instance of {@link LocalDate} reflecting the date
     * the current time - measured in milliseconds since the first January 1970 - corresponds to.
     */
    LocalDate toDate();


    /**
     * Converts the passed time - in the form of a UTC time stamp measuring the milliseconds passed since the
     * first of January 1970 - to an instance of {@link LocalDate}. The time zone applied must be treated as
     * an implementation detail.
     *
     * @param time time passed since the first of the first of January 1970 measured in milliseconds.
     *
     * @return given some implementation-specific time-zone, an instance of {@link LocalDate} reflecting the date
     * the current time - measured in milliseconds since the first January 1970 - corresponds to.
     */
    LocalDate toDate(Long time);

}
