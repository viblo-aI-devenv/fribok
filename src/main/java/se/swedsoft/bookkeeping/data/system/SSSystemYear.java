package se.swedsoft.bookkeeping.data.system;


import se.swedsoft.bookkeeping.data.SSAccountPlan;
import se.swedsoft.bookkeeping.data.SSNewAccountingYear;
import se.swedsoft.bookkeeping.gui.util.table.SSTableSearchable;
import se.swedsoft.bookkeeping.util.SSDateUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.rmi.server.UID;
import java.text.DateFormat;
import java.time.LocalDate;


/**
 * Date: 2006-feb-24
 *
 *  Virual yeardata
 */
public class SSSystemYear implements Serializable, SSTableSearchable {

    // Constant for serialization versioning.
    static final long serialVersionUID = 1L;

    private UID iID;

    private LocalDate iDateFrom;

    private LocalDate iDateTo;

    private String iAccountPlan;

    private boolean iCurrent;

    private transient SSNewAccountingYear iYear;

    /**
     * Creates a new systemyear
     */
    public SSSystemYear() {
        iID = new UID();
        iDateFrom = SSDateUtil.today();
        iDateTo = SSDateUtil.today();
        iCurrent = false;
        iAccountPlan = null;
    }

    /**
     *
     * @param pYear
     */
    public SSSystemYear(SSNewAccountingYear pYear) {
        iYear = pYear;
        iCurrent = false;
        iID = new UID();
        iDateFrom = pYear.getLocalFrom();
        iDateTo = pYear.getLocalTo();
        iAccountPlan = pYear.getAccountPlan() != null
                ? pYear.getAccountPlan().getName()
                : null;

        // unloadYear();
    }

    /**
     * Returns the id for the year
     *
     * @return the id
     */
    public UID getId() {
        return iID;
    }

    // ////////////////////////////////////////////////////////////////////////////

    public LocalDate getLocalFrom() {
        return iDateFrom;
    }

    public void setLocalFrom(LocalDate iDateFrom) {
        this.iDateFrom = iDateFrom;
    }

    // ////////////////////////////////////////////////////////////////////////////

    public LocalDate getLocalTo() {
        return iDateTo;
    }

    public void setLocalTo(LocalDate iDateTo) {
        this.iDateTo = iDateTo;
    }

    // ////////////////////////////////////////////////////////////////////////////

    /**
     * Return the accoutn plans name for the year
     *
     * @return the accountplan
     */
    public String getAccountPlan() {
        return iAccountPlan;
    }

    /**
     *
     * @param iAccountPlan
     */
    public void setAccountPlan(String iAccountPlan) {
        this.iAccountPlan = iAccountPlan;
    }

    /**
     *
     * @param iAccountPlan
     */
    public void setAccountPlan(SSAccountPlan iAccountPlan) {
        this.iAccountPlan = iAccountPlan == null ? null : iAccountPlan.getName();
    }

    // ////////////////////////////////////////////////////////////////////////////

    /**
     *
     * @return
     */
    public SSNewAccountingYear getData() {
        return iYear;
    }

    /**
     *
     * @param iAccountingYear
     */
    public void setData(SSNewAccountingYear iAccountingYear) {

        iYear = iAccountingYear;
    }

    // ////////////////////////////////////////////////////////////////////////////

    public void setCurrentYear(SSNewAccountingYear iAccountingYear) {
        iDateFrom = iAccountingYear.getLocalFrom();
        iDateTo = iAccountingYear.getLocalTo();
        iCurrent = true;
        iAccountPlan = iAccountingYear.getAccountPlan() != null
                ? iAccountingYear.getAccountPlan().getName()
                : null;
        iYear = iAccountingYear;
    }

    /**
     *
     * @return if the year is the current year
     */
    public boolean isCurrent() {
        return iCurrent;
    }

    /**
     * Set if the year shall be the current one, loads data if true, unloads if false
     *
     * @param pCurrent if the year shall be current
     */
    public void setCurrent(boolean pCurrent) {
        iCurrent = pCurrent;

    }

    public boolean equals(Object other) {
        if (other instanceof SSSystemYear) {
            return iID.equals(((SSSystemYear) other).iID);
        }
        if (other instanceof SSNewAccountingYear) {
            return iID.equals(((SSNewAccountingYear) other).getId());
        }
        return false;
    }

    public String toString() {
        DateFormat iFormat = DateFormat.getDateInstance(DateFormat.SHORT);

        StringBuilder sb = new StringBuilder();

        sb.append("SSSystemYear (");
        sb.append(iID);
        sb.append("): ");
        sb.append(iFormat.format(SSDateUtil.toDate(iDateFrom)));
        sb.append("- ");
        sb.append(iFormat.format(SSDateUtil.toDate(iDateTo)));
        sb.append(", ");
        sb.append(iAccountPlan);

        return sb.toString();
    }

    /**
     * Returns the render string to be shown in the tables
     *
     * @return The searchable string
     */
    public String toRenderString() {
        DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);

        return format.format(SSDateUtil.toDate(iDateFrom)) + " - " + format.format(SSDateUtil.toDate(iDateTo));
    }

    /**
     * Custom deserialization to handle old serialized streams that stored Date fields.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();
        iID = (UID) fields.get("iID", null);
        iDateFrom = SSDateUtil.readLocalDate(fields.get("iDateFrom", null));
        iDateTo = SSDateUtil.readLocalDate(fields.get("iDateTo", null));
        iAccountPlan = (String) fields.get("iAccountPlan", null);
        iCurrent = fields.get("iCurrent", false);
        iYear = null;
    }

}
