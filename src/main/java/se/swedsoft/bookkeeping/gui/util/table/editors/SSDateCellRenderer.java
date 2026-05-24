/*
 * 2005-2010
 * $Id$
 */
package se.swedsoft.bookkeeping.gui.util.table.editors;


import javax.swing.table.DefaultTableCellRenderer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;


/**
 * This class implements a cell renderer that renders a {@link LocalDate}.
 *
 * @author Roger Björnstedt
 */
public class SSDateCellRenderer extends DefaultTableCellRenderer {

    // The formatter to use for java.time.LocalDate values.
    private DateTimeFormatter iLocalDateFormat;

    /**
     * Default constructor.
     */
    public SSDateCellRenderer() {
        setHorizontalAlignment(DefaultTableCellRenderer.LEFT);
    }

    /**
     * Sets the value for the cell.
     *
     * @param value The value to format.
     */
    @Override
    public void setValue(Object value) {
        if (value == null) {
            setText("");
        } else if (value instanceof LocalDate) {
            if (iLocalDateFormat == null) {
                iLocalDateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
            }
            setText(iLocalDateFormat.format((LocalDate) value));
        } else {
            setText(value.toString());
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("se.swedsoft.bookkeeping.gui.util.table.editors.SSDateCellRenderer");
        sb.append("{iLocalDateFormat=").append(iLocalDateFormat);
        sb.append('}');
        return sb.toString();
    }
}
