package se.swedsoft.bookkeeping;


import org.hsqldb.Trigger;
import se.swedsoft.bookkeeping.data.system.SSDB;
import se.swedsoft.bookkeeping.data.system.SSDBConfig;

import java.io.*;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * User: Andreas Lago
 * Date: 2007-apr-19
 * Time: 16:21:55
 */
public class SSTriggerHandler extends Thread implements Trigger {    private static final Logger LOG = LoggerFactory.getLogger(SSTriggerHandler.class);

    Socket iSocket;
    public SSTriggerHandler() {
        if (SSDB.getInstance().getLocking()) {
            try {
                iSocket = new Socket(SSDBConfig.getServerAddress(), 2223);
            } catch (IOException e) {
                LOG.error("Unexpected error", e);
            }
        }
    }

    @Override
    public void run() {
        try {
            PrintWriter iOut = new PrintWriter(
                    new OutputStreamWriter(iSocket.getOutputStream()), true);
            BufferedReader iIn = new BufferedReader(
                    new InputStreamReader(iSocket.getInputStream()));

            while (SSDB.getInstance().getLocking()) {

                String iTriggerName = iIn.readLine();
                String iTableName = iIn.readLine();

                String iNumber = iIn.readLine();
                String iCompanyId = iIn.readLine();

                iOut.println("done");

                if (SSDB.getInstance().getCurrentCompany() == null) {
                    continue;
                }
                // LOG.info("Trigger: "+iTableName+" ; "+iTriggerName+" ; "+iNumber+" ; "+iCompanyId);
                if (iCompanyId != null) {
                    Integer iId = Integer.parseInt(iCompanyId);

                    if (iId.equals(SSDB.getInstance().getCurrentCompany().getId())
                            || (iTableName.equals("TBL_VOUCHER")
                                    && SSDB.getInstance().getCurrentYear() != null
                                    && iId.equals(
                                            SSDB.getInstance().getCurrentYear().getId()))) {
                        SSDB.getInstance().triggerAction(iTriggerName, iTableName, iNumber);
                    }
                }
            }
            iOut.close();
            iIn.close();
            iSocket.close();
        } catch (IOException e) {
            LOG.error("Unexpected error", e);
        }
    }

    public void fire(int type, String trigName, String tabName, Object[] oldRow, Object[] newRow) {
        if (type == UPDATE_BEFORE_ROW) {
            oldRow = null;
        }
        String iNumber = null;
        Integer iCompanyId = null;

        if (trigName.contains("PROJECT") || trigName.contains("RESULTUNIT")
                || trigName.contains("VOUCHERTEMPLATE") || trigName.contains("OWNREPORT")) {
            if (oldRow != null) {
                iNumber = oldRow[0].toString();
                iCompanyId = (Integer) oldRow[2];
            }

            if (newRow != null) {
                iNumber = newRow[0].toString();
                iCompanyId = (Integer) newRow[2];
            }
        } else {
            // "Normala objekt"
            if (oldRow != null) {
                iNumber = oldRow[1].toString();
                iCompanyId = (Integer) oldRow[3];
            }

            if (newRow != null) {
                iNumber = newRow[1].toString();
                iCompanyId = (Integer) newRow[3];
            }
        }
        // LOG.info("Trigger: " + trigName + "\nNummer: " + iNumber + "\n\n");

        if (iCompanyId != null && SSDB.getInstance().getCurrentCompany() != null) {

            if (iCompanyId.equals(SSDB.getInstance().getCurrentCompany().getId())
                    || (tabName != null && tabName.equals("TBL_VOUCHER")
                    && SSDB.getInstance().getCurrentYear() != null
                    && iCompanyId.equals(SSDB.getInstance().getCurrentYear().getId()))) {
                SSDB.getInstance().triggerAction(trigName, tabName, iNumber);
            }
        }

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("se.swedsoft.bookkeeping.SSTriggerHandler");
        sb.append("{iSocket=").append(iSocket);
        sb.append('}');
        return sb.toString();
    }
}
