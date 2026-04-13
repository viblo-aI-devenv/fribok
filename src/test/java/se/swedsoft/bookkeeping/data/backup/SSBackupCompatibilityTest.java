package se.swedsoft.bookkeeping.data.backup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.swedsoft.bookkeeping.data.backup.util.SSBackupType;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for {@link SSBackup} serialization compatibility.
 */
class SSBackupCompatibilityTest {

    private static final String LEGACY_BACKUP_CLASS = "se.swedsoft.bookkeeping.data.backup.SSBackup";

    @Test
    void loadBackupReadsLegacyDateBasedSerialization(@TempDir Path tempDir) throws Exception {
        LocalDateTime expectedDate = LocalDateTime.of(2026, 3, 1, 12, 34, 56);
        File backupFile = writeLegacyBackup(tempDir, "legacy-date.zip", expectedDate,
                legacyDateBackupSource(), Date.class, Date.from(expectedDate.atZone(ZoneId.systemDefault()).toInstant()));

        SSBackup backup = SSBackup.loadBackup(backupFile);

        assertThat(backup.getFilename()).isEqualTo("legacy-date.zip");
        assertThat(backup.getType()).isEqualTo(SSBackupType.FULL);
        assertThat(backup.getLocalDateTime()).isEqualTo(expectedDate);
    }

    @Test
    void loadBackupReadsTransientLocalDateTimeSerialization(@TempDir Path tempDir) throws Exception {
        LocalDateTime expectedDate = LocalDateTime.of(2026, 4, 1, 8, 9, 10);
        File backupFile = writeLegacyBackup(tempDir, "legacy-localdatetime.zip", expectedDate,
                legacyLocalDateTimeBackupSource(), LocalDateTime.class, expectedDate);

        SSBackup backup = SSBackup.loadBackup(backupFile);

        assertThat(backup.getFilename()).isEqualTo("legacy-localdatetime.zip");
        assertThat(backup.getType()).isEqualTo(SSBackupType.FULL);
        assertThat(backup.getLocalDateTime()).isEqualTo(expectedDate);
    }

    private File writeLegacyBackup(Path tempDir, String filename, LocalDateTime expectedDate,
            String source, Class<?> dateType, Object serializedDate) throws Exception {
        Path compileDir = Files.createDirectory(tempDir.resolve("legacy-classes-" + filename.replace('.', '-')));
        Path sourceFile = compileDir.resolve("SSBackup.java");
        Files.writeString(sourceFile, source);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertThat(compiler).as("JDK compiler available for compatibility test").isNotNull();

        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
            Iterable<? extends javax.tools.JavaFileObject> compilationUnits =
                    fileManager.getJavaFileObjects(sourceFile.toFile());
            boolean compiled = compiler.getTask(null, fileManager, null,
                    List.of("-classpath", System.getProperty("java.class.path"), "-d",
                            compileDir.toString()), null, compilationUnits).call();
            assertThat(compiled).isTrue();
        }

        File backupFile = tempDir.resolve(filename + ".info").toFile();

        try (LegacyBackupClassLoader classLoader = new LegacyBackupClassLoader(compileDir);
             ObjectOutputStream outputStream = new ObjectOutputStream(
                      new BufferedOutputStream(new FileOutputStream(backupFile)))) {
            Class<?> legacyBackupClass = Class.forName(LEGACY_BACKUP_CLASS, true, classLoader);
            Constructor<?> constructor = legacyBackupClass.getDeclaredConstructor(
                    SSBackupType.class, String.class, dateType);
            Object legacyBackup = constructor.newInstance(SSBackupType.FULL, filename, serializedDate);

            outputStream.writeObject(legacyBackup);
        }

        return backupFile;
    }

    private String legacyDateBackupSource() {
        return "package se.swedsoft.bookkeeping.data.backup;\n"
                + "\n"
                + "import se.swedsoft.bookkeeping.data.backup.util.SSBackupType;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "import java.util.Date;\n"
                + "\n"
                + "public class SSBackup implements Serializable {\n"
                + "    static final long serialVersionUID = 1L;\n"
                + "\n"
                + "    private String iFilename;\n"
                + "    private Date iDate;\n"
                + "    private SSBackupType iType;\n"
                + "\n"
                + "    public SSBackup(SSBackupType iType, String iFilename, Date iDate) {\n"
                + "        this.iType = iType;\n"
                + "        this.iFilename = iFilename;\n"
                + "        this.iDate = iDate;\n"
                + "    }\n"
                + "}\n";
    }

    private String legacyLocalDateTimeBackupSource() {
        return "package se.swedsoft.bookkeeping.data.backup;\n"
                + "\n"
                + "import se.swedsoft.bookkeeping.data.backup.util.SSBackupType;\n"
                + "\n"
                + "import java.io.Serializable;\n"
                + "import java.time.LocalDateTime;\n"
                + "\n"
                + "public class SSBackup implements Serializable {\n"
                + "    static final long serialVersionUID = 1L;\n"
                + "\n"
                + "    private String iFilename;\n"
                + "    private LocalDateTime iDate;\n"
                + "    private SSBackupType iType;\n"
                + "\n"
                + "    public SSBackup(SSBackupType iType, String iFilename, LocalDateTime iDate) {\n"
                + "        this.iType = iType;\n"
                + "        this.iFilename = iFilename;\n"
                + "        this.iDate = iDate;\n"
                + "    }\n"
                + "}\n";
    }

    private static final class LegacyBackupClassLoader extends URLClassLoader {

        private LegacyBackupClassLoader(Path compileDir) throws IOException {
            super(new URL[]{compileDir.toUri().toURL()}, SSBackupCompatibilityTest.class.getClassLoader());
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                Class<?> loadedClass = findLoadedClass(name);

                if (loadedClass == null && LEGACY_BACKUP_CLASS.equals(name)) {
                    try {
                        loadedClass = findClass(name);
                    } catch (ClassNotFoundException ignored) {}
                }

                if (loadedClass == null) {
                    loadedClass = super.loadClass(name, false);
                }

                if (resolve) {
                    resolveClass(loadedClass);
                }
                return loadedClass;
            }
        }
    }
}
