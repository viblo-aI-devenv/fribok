package se.swedsoft.bookkeeping.data;


import se.swedsoft.bookkeeping.util.SSDateUtil;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


/**
 * Date: 2006-jan-27
 * Time: 11:48:46
 */
public class SSMonth  implements Serializable {

    // Constant for serialization versioning.
    static final long serialVersionUID = 1L;

    private LocalDate iFrom;

    private LocalDate iTo;

    /**
     * @deprecated Use {@link #SSMonth(LocalDate)} instead.
     */
    @Deprecated
    public SSMonth(Date pFrom) {
        iFrom = SSDateUtil.toLocalDate(pFrom);
        iTo = null;
    }

    /**
     * @param pFrom the start date as a LocalDate
     */
    public SSMonth(LocalDate pFrom) {
        iFrom = pFrom;
        iTo = null;
    }

    /**
     * @deprecated Use {@link #SSMonth(LocalDate, LocalDate)} instead.
     */
    @Deprecated
    public SSMonth(Date pFrom, Date pTo) {
        iFrom = SSDateUtil.toLocalDate(pFrom);
        iTo = SSDateUtil.toLocalDate(pTo);
    }

    /**
     * @param pFrom the start date as a LocalDate
     * @param pTo the end date as a LocalDate
     */
    public SSMonth(LocalDate pFrom, LocalDate pTo) {
        iFrom = pFrom;
        iTo = pTo;
    }

    /**
     *
     * @return The date
     */
    @Deprecated
    public Date getDate() {
        return SSDateUtil.toDate(iFrom);
    }

    /**
     *
     * @return The date
     */
    @Deprecated
    public Date getFrom() {
        return SSDateUtil.toDate(iFrom);
    }

    /**
     * @return the from date as a LocalDate
     */
    public LocalDate getLocalFrom() {
        return iFrom;
    }

    /**
     *
     * @return The date
     */
    @Deprecated
    public Date getTo() {
        return SSDateUtil.toDate(iTo);
    }

    /**
     * @return the to date as a LocalDate
     */
    public LocalDate getLocalTo() {
        return iTo;
    }

    /**
     *
     * @param pFrom
     * @param pTo
     * @return boolean
     */
    @Deprecated
    public boolean isBetween(Date pFrom, Date pTo) {
        LocalDate localFrom = SSDateUtil.toLocalDate(pFrom);
        LocalDate localTo = SSDateUtil.toLocalDate(pTo);
        return !iFrom.isBefore(localFrom) && !iFrom.isAfter(localTo);
    }

    /**
     * @param pFrom the start date as a LocalDate
     * @param pTo the end date as a LocalDate
     * @return true if this month's from date is between pFrom and pTo (inclusive)
     */
    public boolean isBetween(LocalDate pFrom, LocalDate pTo) {
        return !iFrom.isBefore(pFrom) && !iFrom.isAfter(pTo);
    }

    public int hashCode() {
        if (iFrom == null) {
            return super.hashCode();
        }

        return iFrom.getYear() * 12 + iFrom.getMonthValue();
    }

    public boolean equals(Object obj) {
        if (obj instanceof SSMonth) {
            return obj.hashCode() == hashCode();
        }
        return super.equals(obj);
    }

    public String toString() {
        DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);

        return format.format(SSDateUtil.toDate(iFrom)).substring(0, 7);
    }

    /**
     *
     * @return
     */
    public String getName() {
        int iMonth = iFrom.getMonthValue() - 1;
        int iYear = iFrom.getYear();

        DateFormatSymbols iSymbols = new DateFormatSymbols();

        return iSymbols.getMonths()[iMonth] + ", " + iYear;
    }

    /**
     *  Breaks a year into it's months
     * @param pYearData
     * @return
     */
    public static List<SSMonth> splitYearIntoMonths(SSNewAccountingYear pYearData) {
        Date iFrom = pYearData.getFrom();
        Date iTo = pYearData.getTo();

        return splitYearIntoMonths(iFrom, iTo);
    }

    public boolean isDateInMonth(Date iDate) {
        LocalDate checkDate = SSDateUtil.toLocalDate(iDate);
        return checkDate != null
                && checkDate.getMonth() == iFrom.getMonth()
                && checkDate.getYear() == iFrom.getYear();
    }

    /**
     *
     * Breaks a year into it's months
     *
     * @param iFrom
     * @param iTo
     *
     * @return List of months
     */
    public static List<SSMonth> splitYearIntoMonths(Date iFrom, Date iTo) {
        List<SSMonth> iMonths = new LinkedList<>();

        Calendar iCalendar = Calendar.getInstance();

        // Set the time
        iCalendar.setTime(iFrom);
        // Reset the date to the first day of the month
        iCalendar.set(Calendar.DAY_OF_MONTH, 1);

        // Loop throught all the months between the from date and the to date
        while (iCalendar.getTime().compareTo(iTo) < 0) {
            Date mForm = iCalendar.getTime();

            iCalendar.set(Calendar.DAY_OF_MONTH,
                    iCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));

            Date mTo = iCalendar.getTime();

            SSMonth iMonth = new SSMonth(mForm, mTo);

            iMonths.add(iMonth);

            iCalendar.set(Calendar.DAY_OF_MONTH, 1);
            iCalendar.add(Calendar.MONTH, 1);
        }
        return iMonths;
    }

}
