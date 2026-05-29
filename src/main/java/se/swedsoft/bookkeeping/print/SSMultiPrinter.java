package se.swedsoft.bookkeeping.print;


import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JROrigin;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JRStyle;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import se.swedsoft.bookkeeping.gui.SSMainFrame;
import se.swedsoft.bookkeeping.gui.util.model.SSDefaultTableModel;
import se.swedsoft.bookkeeping.print.util.SSDefaultJasperDataSource;
import se.swedsoft.bookkeeping.print.view.SSJasperPreviewFrame;

import javax.swing.JDialog;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;


/**
 * Date: 2006-mar-02
 * Time: 14:27:30
 */
public class SSMultiPrinter extends SSPrinter {

    private class SSSubReport {

        private String iName;

        private JasperReport iReport;

        private JRDataSource iDataSource;

        private Map<String, Object> iParameters;

        private ResourceBundle iBundle;

        private JasperPrint iPrinter;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();

            sb.append("se.swedsoft.bookkeeping.print.SSMultiPrinter.SSSubReport");
            sb.append("{iBundle=").append(iBundle);
            sb.append(", iDataSource=").append(iDataSource);
            sb.append(", iName='").append(iName).append('\'');
            sb.append(", iParameters=").append(iParameters);
            sb.append(", iReport=").append(iReport);
            sb.append(", iPrinter=").append(iPrinter);
            sb.append('}');
            return sb.toString();
        }
    }

    private List<SSSubReport> iSubReports;

    private JasperPrint iPrinter;

    /**
     *
     */
    public SSMultiPrinter() {
        iSubReports = new LinkedList<>();

        setMargins(0, 0, 0, 0);
        setDetail("multireport.jrxml");
    }

    /**
     * Gets the title file for this repport
     *
     * @return
     */
    @Override
    public String getTitle() {
        if (!iSubReports.isEmpty()) {
            SSSubReport iSubReport = iSubReports.get(0);

            return iSubReport.iName;
        }

        return "";
    }

    /**
     * @return SSDefaultTableModel
     */
    @Override
    protected SSDefaultTableModel getModel() {

        SSDefaultTableModel <SSSubReport>iModel = new SSDefaultTableModel<>() {
            @Override
            public Class<?> getType() {
                return String.class;
            }

            public Object getValueAt(int rowIndex, int columnIndex) {

                SSSubReport iSubReport = getObject(rowIndex);

                Object value = null;

                switch (columnIndex) {
                case 0: // raport.name
                    value = iSubReport.iName;
                    break;

                case 1: // raport.report
                    value = iSubReport.iReport;
                    break;

                case 2: // raport.datasource
                    value = iSubReport.iDataSource;
                    break;

                case 3: // raport.parameters
                    value = iSubReport.iParameters;
                    break;

                case 4: // raport.bundle
                    value = iSubReport.iBundle;
                    break;

                }
                return value;
            }
        };

        iModel.addColumn("report.name");

        iModel.addColumn("report.report");
        iModel.addColumn("report.datasource");
        iModel.addColumn("report.parameters");
        iModel.addColumn("report.bundle");

        iModel.setObjects(iSubReports);

        return iModel;
    }

    /**
     *
     * @param pReport
     */
    public void addReport(SSPrinter pReport) {
        SSSubReport iSubReport = new SSSubReport();

        pReport.generateReport();

        iSubReport.iName = pReport.getTitle();
        iSubReport.iReport = pReport.getReport();
        iSubReport.iDataSource = new SSDefaultJasperDataSource(pReport.getModel());
        iSubReport.iParameters = pReport.getParameters();
        iSubReport.iBundle = pReport.getBundle();
        iSubReport.iPrinter = pReport.getPrinter();

        iSubReports.add(iSubReport);
    }

    /**
     * Generates a combined print by appending the already generated pages from
     * each child report.  Older Bokfri versions nested each report as a
     * subreport in {@code multireport.jrxml}; with newer JasperReports this can
     * drop page/column bands for sales documents and produce blank previews.
     *
     * <p>The combined {@link JasperPrint} must also carry print metadata such
     * as styles, origins, locale, and format factories.  Copying only the pages
     * can leave rendered page elements referring to styles/origins that are not
     * present on the parent print, which makes previews/printing appear blank
     * in some Jasper renderers.</p>
     */
    @Override
    public void generateReport() {
        iPrinter = null;

        for (SSSubReport iSubReport : iSubReports) {
            JasperPrint iSubPrinter = iSubReport.iPrinter;

            if (iSubPrinter == null) {
                continue;
            }
            if (iPrinter == null) {
                iPrinter = new JasperPrint();
                iPrinter.copyFrom(iSubPrinter);
                iPrinter.getPages().clear();
                iPrinter.setBookmarks(new LinkedList<>());
                iPrinter.setName("MultiReport");
            } else {
                mergePrintMetadata(iSubPrinter);
            }
            for (JRPrintPage iPage : iSubPrinter.getPages()) {
                iPrinter.addPage(iPage);
            }
        }

        if (iPrinter == null) {
            iPrinter = new JasperPrint();
            iPrinter.setName("MultiReport");
        }
    }

    private void mergePrintMetadata(JasperPrint iSubPrinter) {
        if (iPrinter.getDefaultStyle() == null && iSubPrinter.getDefaultStyle() != null) {
            iPrinter.setDefaultStyle(iSubPrinter.getDefaultStyle());
        }
        for (JRStyle iStyle : iSubPrinter.getStylesList()) {
            if (!iPrinter.getStylesMap().containsKey(iStyle.getName())) {
                try {
                    iPrinter.addStyle(iStyle);
                } catch (JRException ignored) {}
            }
        }
        for (JROrigin iOrigin : iSubPrinter.getOriginsList()) {
            if (!iPrinter.getOriginsMap().containsKey(iOrigin)) {
                iPrinter.addOrigin(iOrigin);
            }
        }
        iPrinter.getPropertiesMap().copyOwnProperties(iSubPrinter.getPropertiesMap());
    }

    @Override
    public JasperPrint getPrinter() {
        return iPrinter;
    }

    @Override
    public void preview() {
        generateReport();
        net.sf.jasperreports.view.JasperViewer.viewReport(iPrinter, false);
    }

    @Override
    public void preview(SSMainFrame iMainFrame) {
        generateReport();
        showPreview(iMainFrame, null);
    }

    @Override
    public void preview(JDialog iDialog) {
        generateReport();
        SSJasperPreviewFrame iJasperPreviewFrame = new SSJasperPreviewFrame(
                SSMainFrame.getInstance(), 800, 600);

        iJasperPreviewFrame.setInCenter(iDialog);
        iJasperPreviewFrame.setPrinter(iPrinter);
        iJasperPreviewFrame.setVisible(true);
    }

    @Override
    public void preview(SSMainFrame iMainFrame, InternalFrameListener listener) {
        generateReport();
        showPreview(iMainFrame, listener);
    }

    @Override
    public void preview(SSMainFrame iMainFrame, final ActionListener iCloseListener) {
        preview(iMainFrame, new InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                ActionEvent iEvent = new ActionEvent(e.getSource(), e.getID(), "close");

                iCloseListener.actionPerformed(iEvent);
                e.getInternalFrame().removeInternalFrameListener(this);
            }
        });
    }

    private void showPreview(SSMainFrame iMainFrame, InternalFrameListener iListener) {
        SSJasperPreviewFrame iJasperPreviewFrame = new SSJasperPreviewFrame(iMainFrame,
                800, 600);

        if (iListener != null) {
            iJasperPreviewFrame.addInternalFrameListener(iListener);
        }
        iJasperPreviewFrame.setInCenter(iMainFrame);
        iJasperPreviewFrame.setPrinter(iPrinter);
        iJasperPreviewFrame.setVisible(true);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("se.swedsoft.bookkeeping.print.SSMultiPrinter");
        sb.append("{iSubReports=").append(iSubReports);
        sb.append('}');
        return sb.toString();
    }
}
