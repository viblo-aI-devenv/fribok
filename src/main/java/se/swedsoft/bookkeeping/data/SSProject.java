/*
 * 2005-2010
 * $Id$
 */
package se.swedsoft.bookkeeping.data;


import se.swedsoft.bookkeeping.gui.util.table.SSTableSearchable;
import se.swedsoft.bookkeeping.util.SSDateUtil;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;


/**
 */
public class SSProject implements Serializable, SSTableSearchable {

    // Constant for serialization versioning.
    static final long serialVersionUID = 1L;

    private int iNumber;

    private String iName;

    private String iDescription;

    private boolean iConcluded;

    private LocalDate iConcludedDate;

    /**
     * Default constructor.
     */
    public SSProject() {}

    /**
     *
     * @param pNumber
     * @param pName
     * @param pDescription
     */
    public SSProject(int pNumber, String pName, String pDescription) {
        iNumber = pNumber;
        iName = pName;
        iDescription = pDescription;
        iConcluded = false;
        iConcludedDate = null;
    }

    // /////////////////////////////////////////////////////////////////////////

    /**
     *
     */

    // /////////////////////////////////////////////////////////////////////////

    /**
     *
     * @return
     */
    public int getNumber() {
        return iNumber;
    }

    /**
     *
     * @param number
     */
    public void setNumber(int number) {
        iNumber = number;
    }

    // /////////////////////////////////////////////////////////////////////////

    /**
     *
     * @return
     */
    public String getName() {
        return iName;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        iName = name;
    }

    // /////////////////////////////////////////////////////////////////////////

    /**
     *
     * @return
     */
    public String getDescription() {
        return iDescription;
    }

    /**
     *
     * @param description
     */
    public void setDescription(String description) {
        iDescription = description;
    }

    // /////////////////////////////////////////////////////////////////////////

    /**
     *
     * @return
     */
    public boolean getConcluded() {
        return iConcluded;
    }

    /**
     *
     * @param pConcluded
     */
    public void setConcluded(boolean pConcluded) {
        iConcluded = pConcluded;
    }

    // /////////////////////////////////////////////////////////////////////////

    public LocalDate getLocalConcludedDate() {
        return iConcludedDate;
    }

    public void setLocalConcludedDate(LocalDate pConcluded) {
        iConcludedDate = pConcluded;
    }

    // /////////////////////////////////////////////////////////////////////////

    public String toString() {
        DateTimeFormatter iFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);

        StringBuilder sb = new StringBuilder();

        sb.append(iNumber);
        sb.append(" - ");
        sb.append(iName);
        sb.append(", ");
        sb.append(iDescription);
        if (iConcluded) {
            sb.append("(Concluded ");
            sb.append(iConcludedDate.format(iFormat));
            sb.append(") ");
        }
        return sb.toString();
    }

    /**
     *
     * @param obj
     * @return
     */
    public boolean equals(Object obj) {
        if (obj instanceof SSProject) {
            return ((SSProject) obj).iNumber == iNumber;
        }

        return false;
    }

    /**
     * @return
     */
    public String toRenderString() {
        return Integer.toString(iNumber);
    }

    /**
     * Custom deserialization to handle backward compatibility.
     * Pre-migration serialized streams stored {@code iConcludedDate} as
     * {@code java.util.Date}.  This method reads it as a raw object and converts
     * via {@link SSDateUtil#readLocalDate(Object)}.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        ObjectInputStream.GetField fields = in.readFields();
        iNumber = fields.get("iNumber", 0);
        iName = (String) fields.get("iName", null);
        iDescription = (String) fields.get("iDescription", null);
        iConcluded = fields.get("iConcluded", false);
        iConcludedDate = SSDateUtil.readLocalDate(fields.get("iConcludedDate", null));
    }

}
