package se.swedsoft.bookkeeping.print.dialog;


import se.swedsoft.bookkeeping.gui.SSMainFrame;
import se.swedsoft.bookkeeping.gui.util.SSButtonPanel;
import se.swedsoft.bookkeeping.gui.util.datechooser.SSDateChooser;
import se.swedsoft.bookkeeping.gui.util.dialogs.SSDialog;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;


/**
 * $Id$
 *
 */
public class SSPeriodSelectionDialog extends SSDialog {

    private SSDateChooser iTo;

    private SSDateChooser iFrom;

    private SSButtonPanel iButtonPanel;

    private JPanel iPanel;

    /**
     *
     * @param iMainFrame
     * @param iTitle
     */
    public SSPeriodSelectionDialog(SSMainFrame iMainFrame, String iTitle) {
        super(iMainFrame, iTitle);

        setPanel(iPanel);

        iButtonPanel.addCancelActionListener(e -> setModalResult(JOptionPane.CANCEL_OPTION, true));
        iButtonPanel.addOkActionListener(e -> setModalResult(JOptionPane.OK_OPTION, true));

	getRootPane().setDefaultButton(iButtonPanel.getOkButton());
    }

    /**
     *
     * @return
     */
    public LocalDate getTo() {
        return iTo.getLocalDate();
    }

    /**
     *
     * @param to
     */
    public void setTo(LocalDate to) {
        iTo.setLocalDate(to);
    }

    /**
     *
     * @return
     */
    public LocalDate getFrom() {
        return iFrom.getLocalDate();
    }

    /**
     *
     * @param from
     */
    public void setFrom(LocalDate from) {
        iFrom.setLocalDate(from);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("se.swedsoft.bookkeeping.print.dialog.SSPeriodSelectionDialog");
        sb.append("{iButtonPanel=").append(iButtonPanel);
        sb.append(", iFrom=").append(iFrom);
        sb.append(", iPanel=").append(iPanel);
        sb.append(", iTo=").append(iTo);
        sb.append('}');
        return sb.toString();
    }
}
