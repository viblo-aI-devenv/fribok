package se.swedsoft.bookkeeping;


import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
import org.fribok.bookkeeping.app.Path;
import org.fribok.bookkeeping.app.Version;
import se.swedsoft.bookkeeping.data.system.SSCompanyLock;
import se.swedsoft.bookkeeping.data.system.SSDB;
import se.swedsoft.bookkeeping.data.system.SSDBConfig;
import se.swedsoft.bookkeeping.data.system.SSYearLock;
import se.swedsoft.bookkeeping.data.util.SSConfig;
import se.swedsoft.bookkeeping.gui.SSMainFrame;
import se.swedsoft.bookkeeping.gui.company.SSCompanyFrame;
import se.swedsoft.bookkeeping.gui.util.dialogs.SSQueryDialog;
import se.swedsoft.bookkeeping.gui.util.frame.SSFrameManager;
import se.swedsoft.bookkeeping.gui.util.graphics.SSIcon;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @version $Id$
 */
public class SSBookkeeping {    private static final Logger LOG = LoggerFactory.getLogger(SSBookkeeping.class);


    public static boolean iRunning;

    private SSBookkeeping() {}

    /**
     *
     */
    private static void startupDatabase() {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            LOG.info("ERROR: failed to load HSQLDB JDBC driver.");
            LOG.error("Unexpected error", e);
            return;
        }

        try {
            String iServerAddress = SSDBConfig.getServerAddress();

            if (iServerAddress.length() == 0) {
                // K�r mot lokal databas! Ingen l�sning m.m.
                Connection iConnection = DriverManager.getConnection(
                        "jdbc:hsqldb:file:db" + File.separator + "JFSDB", "sa", "");

                SSDB.getInstance().startupLocal(iConnection);
            } else {
                // K�r mot en JFSServer specificerad i serveradressen.
                Connection iConnection = DriverManager.getConnection(
                        "jdbc:hsqldb:hsql://" + iServerAddress + "/JFSDB", "sa", "");

                SSDB.getInstance().startupRemote(iConnection, iServerAddress);
            }

        } catch (SQLException e) {
            SSQueryDialog iDialog = new SSQueryDialog(SSMainFrame.getInstance(),
                    "noserverfoundstartup");

            if (iDialog.getResponce() == JOptionPane.YES_OPTION) {
                if (SSDB.getInstance().getLocking()) {
                    SSCompanyLock.removeLock(SSDB.getInstance().getCurrentCompany());
                    SSYearLock.removeLock(SSDB.getInstance().getCurrentYear());
                }
                SSDBConfig.setServerAddress(null);
                SSDB.getInstance().setCurrentCompany(null);
                SSDB.getInstance().setCurrentYear(null);
                SSFrameManager.getInstance().close();
                SSDB.getInstance().loadLocalDatabase();
                SSCompanyFrame.showFrame(SSMainFrame.getInstance(), 500, 300);
            }
        }
    }

    /**
     * The main method of the program.
     *
     * @param args The arguments to the program.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            LOG.error("Unexpected error", e);
        }
        iRunning = true;

        // Print information to ease debugging
        LOG.info("Starting up...");
        LOG.info("Title : " + Version.APP_TITLE);
        LOG.info("Version : " + Version.APP_VERSION);
        LOG.info("Build : " + Version.APP_BUILD);
        LOG.info("Directory : " + Path.get(Path.APP_BASE));
        LOG.info("");
        LOG.info("Operating system: " + System.getProperty("os.name"));
        LOG.info("Architecture : " + System.getProperty("os.arch"));
        LOG.info("Java version : " + System.getProperty("java.version"));
        LOG.info("");
        LOG.info("Paths:");
        for (Path name : Path.values()) {
            System.out.printf("   %-12s = %s\n", name, Path.get(name));
        }

        String warning = null;

        // Create paths as needed, warning the user on failure
        for (Path name : Path.values()) {
            File dir = Path.get(name);

            if (!dir.exists()) {
                try {
                    if (dir.mkdirs()) {
                        LOG.info("Created " + dir);
                    } else {
                        warning = "unable to create";
                    }
                } catch (SecurityException e) {
                    LOG.error("Unexpected error", e);
                }
            } else if (!dir.isDirectory()) {
                warning = "exists but is not a directory";
            }
            if (warning != null) {
                LOG.info(" !! WARNING: " + dir + ' ' + warning);
                warning = null;
            }
        }

        // Create and display the main iMainFrame.
        SSMainFrame iMainFrame = SSMainFrame.getInstance();

        UIManager.put("InternalFrame.icon", SSIcon.getIcon("ICON_FRAME"));
        UIManager.put("InternalFrame.inactiveIcon", SSIcon.getIcon("ICON_FRAME"));
        startupDatabase();

        // Display the main frame.
        iMainFrame.setVisible(true);

        SSDB.getInstance().readOldDatabase();

        // Only display the company iMainFrame if there are no companies defined.
        // I would prefer to only open the select company iMainFrame if there are no companies.
        // But Fredrik and Joakim wants it to displayed every time.
        if ((Boolean) SSConfig.getInstance().get("companyframe.showatstart", true)) {
            iMainFrame.showCompanyFrame();
        }

        SSDB.getInstance().init(true);

        // Perhaps add some type of shut down hook.
        Runtime.getRuntime().addShutdownHook(
                new Thread(
                        () -> {

                                SSFrameManager.getInstance().storeAllFrames();

                                if (SSDBConfig.getClientkey() != null
                                        && SSDBConfig.getClientkey().length() != 0) {
                                    SSDB.getInstance().removeClient();
                                }

                                try {
                                    iRunning = false;
                                    SSDB.getInstance().shutdown();
                                    // Shut down the database.
                                    if (SSDB.getInstance().getLocking()
                                            && SSDB.getInstance().getSocket() != null) {
                                        SSDB.getInstance().getWriter().println("disconnect");
                                        SSDB.getInstance().getWriter().flush();
                                        SSDB.getInstance().getWriter().close();

                                        SSDB.getInstance().getReader().close();
                                        SSDB.getInstance().getSocket().close();
                                    }

                                } catch (IOException e) {
                                    LOG.error("Unexpected error", e);
                                }

                            }));
    }

}
