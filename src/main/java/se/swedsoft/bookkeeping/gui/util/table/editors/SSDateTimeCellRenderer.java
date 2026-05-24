/*
 * 2005-2010
 * $Id$
 */
package se.swedsoft.bookkeeping.gui.util.table.editors;


import javax.swing.table.DefaultTableCellRenderer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;


/**
 * This class implements a cell renderer for date and time values.
 */
public class SSDateTimeCellRenderer extends DefaultTableCellRenderer {

    private DateTimeFormatter iDateTimeFormat;
    private DateTimeFormatter iDateFormat;

    /**
     * Default constructor.
     */
    public SSDateTimeCellRenderer() {}

    /**
     * Sets the value for the cell. Accepts {@link java.time.LocalDate} and
     * {@link java.time.LocalDateTime} values.
     *
     * @param value The value to format.
     */
    @Override
    public void setValue(Object value) {
        if (value == null) {
            setText("");
        } else if (value instanceof LocalDateTime) {
            if (iDateTimeFormat == null) {
                iDateTimeFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
            }
            setText(iDateTimeFormat.format((LocalDateTime) value));
        } else if (value instanceof LocalDate) {
            if (iDateFormat == null) {
                iDateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
            }
            setText(iDateFormat.format((LocalDate) value));
        } else {
            setText(value.toString());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("se.swedsoft.bookkeeping.gui.util.table.editors.SSDateTimeCellRenderer");
        sb.append("{iDateTimeFormat=").append(iDateTimeFormat);
        sb.append(", iDateFormat=").append(iDateFormat);
        sb.append('}');
        return sb.toString();
    }
}
