package se.swedsoft.bookkeeping.data;

import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link SSMonth}.
 */
class SSMonthTest {

    private static Date makeDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    // ---- Constructors and getters ----

    @Test
    void singleDateConstructorSetsFromAndToNull() {
        Date date = makeDate(2024, 6, 1);
        SSMonth month = new SSMonth(date);

        assertThat(month.getFrom()).isEqualTo(date);
        assertThat(month.getDate()).isEqualTo(date);
        assertThat(month.getTo()).isNull();
    }

    @Test
    void twoDateConstructorSetsBothDates() {
        Date from = makeDate(2024, 6, 1);
        Date to = makeDate(2024, 6, 30);
        SSMonth month = new SSMonth(from, to);

        assertThat(month.getFrom()).isEqualTo(from);
        assertThat(month.getTo()).isEqualTo(to);
    }

    // ---- isBetween ----

    @Test
    void isBetweenReturnsTrueWhenFromDateIsInRange() {
        Date monthDate = makeDate(2024, 6, 15);
        SSMonth month = new SSMonth(monthDate);

        Date rangeFrom = makeDate(2024, 6, 1);
        Date rangeTo = makeDate(2024, 6, 30);

        assertThat(month.isBetween(rangeFrom, rangeTo)).isTrue();
    }

    @Test
    void isBetweenReturnsTrueWhenFromDateEqualsRangeStart() {
        Date monthDate = makeDate(2024, 6, 1);
        SSMonth month = new SSMonth(monthDate);

        Date rangeFrom = makeDate(2024, 6, 1);
        Date rangeTo = makeDate(2024, 6, 30);

        assertThat(month.isBetween(rangeFrom, rangeTo)).isTrue();
    }

    @Test
    void isBetweenReturnsTrueWhenFromDateEqualsRangeEnd() {
        Date monthDate = makeDate(2024, 6, 30);
        SSMonth month = new SSMonth(monthDate);

        Date rangeFrom = makeDate(2024, 6, 1);
        Date rangeTo = makeDate(2024, 6, 30);

        assertThat(month.isBetween(rangeFrom, rangeTo)).isTrue();
    }

    @Test
    void isBetweenReturnsFalseWhenFromDateIsOutsideRange() {
        Date monthDate = makeDate(2024, 7, 15);
        SSMonth month = new SSMonth(monthDate);

        Date rangeFrom = makeDate(2024, 6, 1);
        Date rangeTo = makeDate(2024, 6, 30);

        assertThat(month.isBetween(rangeFrom, rangeTo)).isFalse();
    }

    // ---- isDateInMonth ----

    @Test
    void isDateInMonthReturnsTrueForSameMonth() {
        SSMonth month = new SSMonth(makeDate(2024, 6, 1));

        assertThat(month.isDateInMonth(makeDate(2024, 6, 15))).isTrue();
    }

    @Test
    void isDateInMonthReturnsFalseForDifferentMonth() {
        SSMonth month = new SSMonth(makeDate(2024, 6, 1));

        assertThat(month.isDateInMonth(makeDate(2024, 7, 15))).isFalse();
    }

    @Test
    void isDateInMonthReturnsFalseForDifferentYear() {
        SSMonth month = new SSMonth(makeDate(2024, 6, 1));

        assertThat(month.isDateInMonth(makeDate(2025, 6, 15))).isFalse();
    }

    // ---- equals and hashCode ----

    @Test
    void equalMonthsSameYearAndMonth() {
        SSMonth a = new SSMonth(makeDate(2024, 6, 1));
        SSMonth b = new SSMonth(makeDate(2024, 6, 15));

        assertThat(a.equals(b)).isTrue();
    }

    @Test
    void differentMonthsNotEqual() {
        SSMonth a = new SSMonth(makeDate(2024, 6, 1));
        SSMonth b = new SSMonth(makeDate(2024, 7, 1));

        assertThat(a.equals(b)).isFalse();
    }

    @Test
    void hashCodeConsistentForSameMonth() {
        SSMonth a = new SSMonth(makeDate(2024, 6, 1));
        SSMonth b = new SSMonth(makeDate(2024, 6, 20));

        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void notEqualToDifferentType() {
        SSMonth month = new SSMonth(makeDate(2024, 6, 1));

        assertThat(month.equals("not a month")).isFalse();
    }

    // ---- getName ----

    @Test
    void getNameContainsYear() {
        SSMonth month = new SSMonth(makeDate(2024, 6, 1));

        assertThat(month.getName()).contains("2024");
    }

    // ---- splitYearIntoMonths ----

    @Test
    void splitYearIntoMonthsReturnsCorrectNumberOfMonths() {
        Date from = makeDate(2024, 1, 1);
        Date to = makeDate(2024, 12, 31);

        List<SSMonth> months = SSMonth.splitYearIntoMonths(from, to);

        assertThat(months).hasSize(12);
    }

    @Test
    void splitYearIntoMonthsSetsFromAndToForEachMonth() {
        Date from = makeDate(2024, 1, 1);
        Date to = makeDate(2024, 3, 31);

        List<SSMonth> months = SSMonth.splitYearIntoMonths(from, to);

        assertThat(months).hasSize(3);

        // Each month should have both from and to set
        for (SSMonth m : months) {
            assertThat(m.getFrom()).isNotNull();
            assertThat(m.getTo()).isNotNull();
        }
    }

    @Test
    void splitYearIntoMonthsFirstMonthStartsOnFirstDay() {
        Date from = makeDate(2024, 1, 1);
        Date to = makeDate(2024, 12, 31);

        List<SSMonth> months = SSMonth.splitYearIntoMonths(from, to);

        Calendar cal = Calendar.getInstance();
        cal.setTime(months.get(0).getFrom());

        assertThat(cal.get(Calendar.MONTH)).isEqualTo(Calendar.JANUARY);
        assertThat(cal.get(Calendar.DAY_OF_MONTH)).isEqualTo(1);
    }

    @Test
    void splitYearIntoMonthsHandlesPartialYear() {
        Date from = makeDate(2024, 7, 1);
        Date to = makeDate(2024, 12, 31);

        List<SSMonth> months = SSMonth.splitYearIntoMonths(from, to);

        assertThat(months).hasSize(6);
    }

    @Test
    void splitYearIntoMonthsHandlesBrokenFiscalYear() {
        // A fiscal year from September to August
        Date from = makeDate(2023, 9, 1);
        Date to = makeDate(2024, 8, 31);

        List<SSMonth> months = SSMonth.splitYearIntoMonths(from, to);

        assertThat(months).hasSize(12);
    }
}
