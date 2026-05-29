package se.swedsoft.bookkeeping.print;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import se.swedsoft.bookkeeping.data.SSNewProject;
import se.swedsoft.bookkeeping.data.system.SSDBTestFixture;
import se.swedsoft.bookkeeping.print.report.SSProjectsPrinter;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for report preview rendering.
 */
@Tag("integration")
class ReportRenderingIntegrationTest {

    @BeforeAll
    static void openDatabase() throws Exception {
        SSDBTestFixture.setupOnce();
    }

    @Test
    void projectReportPreviewImageContainsRenderedContent() throws Exception {
        SSProjectsPrinter printer = new SSProjectsPrinter(List.of(
                new SSNewProject("P-1", "Preview project", "Ensures report pages are not blank")));

        printer.generateReport();

        JasperPrint jasperPrint = printer.getPrinter();
        Image image = JasperPrintManager.printPageToImage(jasperPrint, 0, 1.0f);
        BufferedImage bufferedImage = toBufferedImage(image);

        assertThat(countNonWhitePixels(bufferedImage)).isGreaterThan(1_000);
    }

    private static BufferedImage toBufferedImage(Image image) {
        if (image instanceof BufferedImage) {
            return (BufferedImage) image;
        }
        BufferedImage bufferedImage = new BufferedImage(
                image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);

        bufferedImage.getGraphics().drawImage(image, 0, 0, null);
        return bufferedImage;
    }

    private static int countNonWhitePixels(BufferedImage image) {
        int count = 0;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y) & 0x00ffffff;

                if (rgb != 0x00ffffff) {
                    count++;
                }
            }
        }
        return count;
    }
}
