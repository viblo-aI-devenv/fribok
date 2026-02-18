package se.swedsoft.bookkeeping.calc.math;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SSDateMath}.
 */
class SSDateMathTest {

    /**
     * Helper: create a Date for the given year/month/day at the given hour/min/sec.
     */
    private static Date makeDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1); // Calendar months are 0-based
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private static Date makeDate(int year, int month, int day) {
        return makeDate(year, month, day, 12, 0, 0);
    }

    // ---- floor ----

    @Test
    void floorSetsTimeTo000000() {
        Date input = makeDate(2024, 6, 15, 14, 30, 45);
        Date result = SSDateMath.floor(input);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(0);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(0);
        assertThat(cal.get(Calendar.SECOND)).isEqualTo(0);
        assertThat(cal.get(Calendar.MILLISECOND)).isEqualTo(0);
    }

    @Test
    void floorPreservesDatePart() {
        Date input = makeDate(2024, 6, 15, 14, 30, 45);
        Date result = SSDateMath.floor(input);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertThat(cal.get(Calendar.YEAR)).isEqualTo(2024);
        assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.JUNE);
        assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(15);
    }

    // ---- ceil ----

    @Test
    void ceilSetsTimeTo235959() {
        Date input = makeDate(2024, 6, 15, 10, 0, 0);
        Date result = SSDateMath.ceil(input);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(23);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(59);
        assertThat(cal.get(Calendar.SECOND)).isEqualTo(59);
        assertThat(cal.get(Calendar.MILLISECOND)).isEqualTo(999);
    }

    @Test
    void ceilPreservesDatePart() {
        Date input = makeDate(2024, 6, 15, 10, 0, 0);
        Date result = SSDateMath.ceil(input);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertThat(cal.get(Calendar.YEAR)).isEqualTo(2024);
        assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.JUNE);
        assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(15);
    }

    // ---- getFirstDayInMonth ----

    @Test
    void getFirstDayInMonthReturnsFirstDay() {
        Date input = makeDate(2024, 6, 15);
        Date result = SSDateMath.getFirstDayInMonth(input);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(1);
        assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.JUNE);
        assertThat(cal.get(Calendar.YEAR)).isEqualTo(2024);
    }

    @Test
    void getFirstDayInMonthSetsTimeToMidnight() {
        Date input = makeDate(2024, 6, 15, 14, 30, 0);
        Date result = SSDateMath.getFirstDayInMonth(input);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(0);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(0);
        assertThat(cal.get(Calendar.SECOND)).isEqualTo(0);
    }

    // ---- getLastDayMonth ----

    @Test
    void getLastDayMonthReturnsLastDay() {
        Date input = makeDate(2024, 6, 10);
        Date result = SSDateMath.getLastDayMonth(input);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(30); // June has 30 days
        assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.JUNE);
    }

    @Test
    void getLastDayMonthForFebruaryLeapYear() {
        Date input = makeDate(2024, 2, 5); // 2024 is a leap year
        Date result = SSDateMath.getLastDayMonth(input);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(29);
    }

    @Test
    void getLastDayMonthForFebruaryNonLeapYear() {
        Date input = makeDate(2023, 2, 5); // 2023 is not a leap year
        Date result = SSDateMath.getLastDayMonth(input);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(28);
    }

    @Test
    void getLastDayMonthSetsTimeToEndOfDay() {
        Date input = makeDate(2024, 6, 10, 10, 0, 0);
        Date result = SSDateMath.getLastDayMonth(input);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertThat(cal.get(Calendar.HOUR_OF_DAY)).isEqualTo(23);
        assertThat(cal.get(Calendar.MINUTE)).isEqualTo(59);
        assertThat(cal.get(Calendar.SECOND)).isEqualTo(59);
    }

    // ---- getDaysBetween ----

    @Test
    void getDaysBetweenForSameDate() {
        Date date = makeDate(2024, 6, 15);

        assertThat(SSDateMath.getDaysBetween(date, date)).isEqualTo(0);
    }

    @Test
    void getDaysBetweenForConsecutiveDays() {
        Date from = makeDate(2024, 6, 15, 0, 0, 0);
        Date to = makeDate(2024, 6, 16, 0, 0, 0);

        assertThat(SSDateMath.getDaysBetween(from, to)).isEqualTo(1);
    }

    @Test
    void getDaysBetweenForOneWeek() {
        Date from = makeDate(2024, 6, 1, 0, 0, 0);
        Date to = makeDate(2024, 6, 8, 0, 0, 0);

        assertThat(SSDateMath.getDaysBetween(from, to)).isEqualTo(7);
    }

    @Test
    void getDaysBetweenReturnsZeroWhenToBeforeFrom() {
        Date from = makeDate(2024, 6, 15);
        Date to = makeDate(2024, 6, 10);

        assertThat(SSDateMath.getDaysBetween(from, to)).isEqualTo(0);
    }

    // ---- getMonthsBetween ----

    @Test
    void getMonthsBetweenReturnsZeroWhenFromBeforeTo() {
        // The method returns 0 when iFrom.before(iTo) == true
        Date from = makeDate(2024, 1, 1);
        Date to = makeDate(2024, 6, 1);

        assertThat(SSDateMath.getMonthsBetween(from, to)).isEqualTo(0);
    }

    @Test
    void getMonthsBetweenCountsMonthsWhenFromAfterTo() {
        // When iFrom is after iTo, it counts months from iFrom forward until reaching iTo
        // But since from > to and the while loop checks iCalendar.before(iTo), it never enters
        Date from = makeDate(2024, 6, 1);
        Date to = makeDate(2024, 1, 1);

        // from.before(to) is false, so the early return is skipped
        // Then loop: from is not before to, so loop doesn't execute
        assertThat(SSDateMath.getMonthsBetween(from, to)).isEqualTo(0);
    }

    // ---- addMonths ----

    @Test
    void addMonthsAddsPositiveMonths() {
        Date input = makeDate(2024, 1, 15);
        Date result = SSDateMath.addMonths(input, 3);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.APRIL);
        assertThat(cal.get(Calendar.YEAR)).isEqualTo(2024);
    }

    @Test
    void addMonthsSubtractsNegativeMonths() {
        Date input = makeDate(2024, 6, 15);
        Date result = SSDateMath.addMonths(input, -3);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.MARCH);
        assertThat(cal.get(Calendar.YEAR)).isEqualTo(2024);
    }

    @Test
    void addMonthsWrapsToNextYear() {
        Date input = makeDate(2024, 11, 15);
        Date result = SSDateMath.addMonths(input, 3);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.FEBRUARY);
        assertThat(cal.get(Calendar.YEAR)).isEqualTo(2025);
    }

    @Test
    void addMonthsWithZeroReturnsSameMonth() {
        Date input = makeDate(2024, 6, 15);
        Date result = SSDateMath.addMonths(input, 0);

        Calendar cal = Calendar.getInstance();
        cal.setTime(result);

        assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.JUNE);
        assertThat(cal.get(Calendar.YEAR)).isEqualTo(2024);
    }
}
