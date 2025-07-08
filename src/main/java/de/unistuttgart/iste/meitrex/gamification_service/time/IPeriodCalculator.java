package de.unistuttgart.iste.meitrex.gamification_service.time;

import de.unistuttgart.iste.meitrex.gamification_service.persistence.entity.LeaderboardEntity;

import java.time.LocalDate;

/**
 * Specifies functionality for period-oriented time-zone-agnostic date calculations.
 *
 * @author Philipp Kunz
 */
public interface IPeriodCalculator {

    /**
     * Maps the passed timestamp onto an instance of {@link LocalDate} marking a period's begin. Any WEEKLY period starts
     * on the closest preceding Monday. If according to the systems default time timestamp corresponds to Monday , the
     * respective date is returned. Analogously, for period type MONTHLY the first day of the month is returned.
     *
     * @param timestamp an UTC timestamp.
     * @param period a period descriptor - must be WEEKLY or MONTHLY.
     * @throws NullPointerException if period is null.
     * @throws IllegalArgumentException if period is ALL_TIME or timestamp is negative.
     * @return an instance of {@link LocalDate} marking the periods begin.
     */
    LocalDate calcStartDate(long timestamp, Period period);

    /**
     * Given a WEEKLY or MONTHLY period whose begin is represented by the passed instance of {@link LocalDate}, this
     * method calculates the start date of the subsequent MONTHLY or WEEKLY period. If period is WEEKLY, the passed
     * start date must represent a Monday, otherwise, if period is MONTHLY it must correspond to the first of month.
     *
     * @param startDate the start date of the current period.
     * @param period a period descriptor.
     * @throws NullPointerException if startDate or period is null.
     * @throws IllegalArgumentException if period is WEEKLY and the passed instance of startDate does not correspond to
     * a Monday, analogously, if period is MONTHLY and the passed instance does not correspond to the first of a month.
     * @return the start date of the subsequent period as an instance of {@link LocalDate}.
     */
    LocalDate calcSucStartDate(LocalDate startDate, Period period);
}
