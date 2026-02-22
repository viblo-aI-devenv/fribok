package se.swedsoft.bookkeeping.data.system;


import org.fribok.bookkeeping.app.Path;
import se.swedsoft.bookkeeping.data.SSNewCompany;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Johan Gunnarsson
 * Date: 2007-jan-26
 * Time: 11:40:47
 */
public class SSCompanyConfig {    private static final Logger LOG = LoggerFactory.getLogger(SSCompanyConfig.class);

    private SSCompanyConfig() {}

    public static void saveLastOpenCompany(SSSystemCompany iLastCompany) {
        File iFile = new File(Path.get(Path.APP_BASE), "lastcompanyopen.config");

        if (iFile.exists()) {
            iFile.delete();
        }
        try (FileOutputStream fos = new FileOutputStream(iFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(iLastCompany);
            if (iLastCompany.getCurrentYear() != null) {
                oos.writeObject(iLastCompany.getCurrentYear());
            }
        } catch (IOException e) {
            LOG.error("Unexpected error", e);
        }
    }

    public static SSSystemCompany openLastOpenCompany() {
        File iFile = new File(Path.get(Path.APP_BASE), "lastcompanyopen.config");

        if (!iFile.exists()) {
            return null;
        }

        SSSystemCompany iSystemCompany = null;

        try (FileInputStream fis = new FileInputStream(iFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            iSystemCompany = (SSSystemCompany) ois.readObject();
            SSSystemYear iSystemYear = (SSSystemYear) ois.readObject();

            if (iSystemCompany != null && iSystemYear != null) {
                for (SSSystemYear iCurrent : iSystemCompany.getYears()) {
                    if (iCurrent.getId().equals(iSystemYear.getId())) {
                        iCurrent.setCurrent(true);
                    }
                }
            }

        } catch (IOException e) {
            iFile.delete();
            return iSystemCompany;
        } catch (ClassNotFoundException e) {
            LOG.error("Unexpected error", e);
        }
        return iSystemCompany;
    }

    public static void saveCompanySetting(SSNewCompany iCompany) {
        if (iCompany == null) {
            return;
        }

        File iFile = new File(Path.get(Path.APP_BASE), "companysettings.config");

        Collection<Object> iVector = new ArrayList<>();

        iVector.add(iCompany);
        // iVector.add(iCompany.getCurrentYear());

        if (iFile.exists()) {
            try (FileInputStream fis = new FileInputStream(iFile);
                 ObjectInputStream ois = new ObjectInputStream(fis)) {
                while (true) {
                    SSSystemCompany iSystemCompany = (SSSystemCompany) ois.readObject();
                    SSSystemYear iSystemYear = (SSSystemYear) ois.readObject();

                    if (iSystemCompany != null) {
                        if (!iSystemCompany.getId().equals(iCompany.getId())) {
                            iVector.add(iSystemCompany);
                            iVector.add(iSystemYear);
                        }
                    }
                }
            } catch (IOException e) {
                LOG.error("Unexpected error", e);
            } catch (ClassNotFoundException e) {
                LOG.error("Unexpected error", e);
            }
        }

        try {
            if (iFile.exists()) {
                iFile.delete();
            }
            try (FileOutputStream fos = new FileOutputStream(iFile);
                 ObjectOutputStream oos = new ObjectOutputStream(fos)) {
                for (Object iObject : iVector) {
                    oos.writeObject(iObject);
                }
            }
        } catch (IOException e) {
            LOG.error("Unexpected error", e);
        }
    }

    public static SSSystemCompany openCompanySetting(SSSystemCompany iCompany) {
        File iFile = new File(Path.get(Path.APP_BASE), "companysettings.config");

        if (!iFile.exists()) {
            return null;
        }

        SSSystemCompany iSystemCompany = null;

        try (FileInputStream fis = new FileInputStream(iFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            SSSystemYear iSystemYear = null;

            while (true) {
                iSystemCompany = (SSSystemCompany) ois.readObject();
                iSystemYear = (SSSystemYear) ois.readObject();
                if (iSystemCompany != null) {
                    if (iSystemCompany.getId().equals(iCompany.getId())) {
                        if (iSystemYear != null) {
                            for (SSSystemYear iCurrent : iSystemCompany.getYears()) {
                                if (iCurrent.getId().equals(iSystemYear.getId())) {
                                    iCurrent.setCurrent(true);
                                }
                            }
                        }
                        return iSystemCompany;
                    }
                }
            }

        } catch (IOException e) {
            return null;
        } catch (ClassNotFoundException e) {
            LOG.error("Unexpected error", e);
        }
        return iSystemCompany;
    }

    private static ObjectOutputStream appendableObjectOutputStream(File f) throws IOException {
        FileOutputStream fos = new FileOutputStream(f, true);
        boolean append = f.exists() && f.length() > 0;

        if (append) {
            return new ObjectOutputStream(fos) {
                @SuppressWarnings({ "RedundantThrowsDeclaration"})
                @Override
                protected void writeStreamHeader() throws IOException {}
            };
        } else {
            return new ObjectOutputStream(fos);
        }
    }

    public static void deleteFiles() {
        File iFile1 = new File(Path.get(Path.APP_BASE), "lastcompanyopen.config");
        File iFile2 = new File(Path.get(Path.APP_BASE), "companysettings.config");

        if (iFile1.exists()) {
            iFile1.delete();
        }

        if (iFile2.exists()) {
            iFile2.delete();
        }

    }
}
