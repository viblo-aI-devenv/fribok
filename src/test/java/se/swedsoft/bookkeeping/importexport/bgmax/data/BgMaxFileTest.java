package se.swedsoft.bookkeeping.importexport.bgmax.data;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * Tests for BGMax.
 *
 * @author jensli
 */
class BgMaxFileTest {
    private static final int NUM_FILES = 4;

    private static final String FILE_NAME = "BgMaxTestFile";
    private static final String FILE_ENDING = "ut";

    private final List<List<String>> files = new ArrayList<>();

    @BeforeEach
    void setUp() throws URISyntaxException, IOException {
        // Load file lines to a list for easy access in tests
        for (int i = 0; i < NUM_FILES; i++) {
            String filename = FILE_NAME + (i + 1) + "." + FILE_ENDING;
            URL url = getClass().getResource(filename);
            File file = new File(url.toURI());
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                List<String> lines = new ArrayList<>();
                String line;

                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                files.add(lines);
            }
        }
    }

    @Test
    void testParseFile3() {
        BgMaxFile bgMaxFile = new BgMaxFile();

        bgMaxFile.parse(files.get(3));

        assertEquals("BGMAX", bgMaxFile.iLayoutnamn, "layout name after reading");
        assertEquals("01", bgMaxFile.iVersion, "iVersion");
        assertEquals("20040525173035010331", bgMaxFile.iTidsstampel, "iTidsstampel");

        assertEquals("00000009", bgMaxFile.iAntalBetalningsPoster,
                "iAntalBetalningsPoster");
        assertEquals("00000000", bgMaxFile.iAntalAvdragsPoster, "iAntalAvdragsPoster");
        assertEquals("00000013", bgMaxFile.iAntalExtraReferensPoster,
                "iAntalExtraReferensPoster");
        assertEquals("00000004", bgMaxFile.iAntalInsattningsPoster,
                "iAntalInsattningsPoster");

        BgMaxAvsnitt avs1 = bgMaxFile.iAvsnitts.get(0);

        assertEquals("0009912346", avs1.iBankgiroNummer, "iBankgiroNummer");
        assertEquals("SEK", avs1.iValuta, "iValuta");

        BgMaxBetalning bet1 = avs1.iBetalningar.get(0);

        assertEquals("0003783511", bet1.iBankgiroNummer, "iBankgiroNummer");
        assertEquals("", bet1.iReferens, "iReferens");
        assertEquals("000000000000180000", bet1.iBelopp, "iBetalningsBelopp");
        assertEquals("0", bet1.iReferensKod, "iReferensKod");
        assertEquals("2", bet1.iBetalningsKanalKod, "iBetalningsKanalKod");
        assertEquals("000120000018", bet1.iBGCLopnummer, "iBGCLopnummer");
        assertEquals("0", bet1.iAvibildmarkering, "iAvibildmarkering");
        assertEquals("Betalning med extra refnr 665869 657775 665661665760",
                bet1.iInformationsText, "iInformationsText");
        assertEquals("Kalles Pl", bet1.iBetalarensNamn.substring(0, 9),
                "iBetalarensNamn");
        assertEquals("Storgatan 2", bet1.iBetalarensAdress, "iBetalarensAdress");
        assertEquals("12345", bet1.iBetalarensPostnummer, "iBetalarensPostnummer");
        assertEquals("Stor", bet1.iBetalarensOrt.substring(0, 4), "iBetalarensOrt");
        assertEquals("005500001234", bet1.iBetalarensOrganisationsnr,
                "iBetalarensOrganisationsnr");

        assertEquals("00000000000000000005841000001009823", avs1.iBankKontoNummer,
                "iBankKontoNummer");
        assertEquals("20040525", avs1.iBetalningsdag, "iBetalningsdag");
        assertEquals("00056", avs1.iLopnummer, "iLopnummer");

        assertEquals("000000000000370", avs1.iBelopp, "iBelopp");
        assertEquals("SEK", avs1.iValuta, "iValuta");
        assertEquals("00000002", avs1.iAntal, "iAntal");

        BgMaxReferens ref1 = bet1.iReferenser.get(0);

        assertEquals("0003783511", ref1.iBankgiroNummer, "iBankgiroNummer");
        assertEquals("665760", ref1.iReferens, "iReferens");
        assertEquals("000000000000000000", ref1.iBelopp, "iBelopp");
        assertEquals("2", ref1.iReferensKod, "iReferensKod");
        assertEquals("2", ref1.iBetalningsKanalKod, "iBetalningsKanalKod");
        assertEquals("000120000018", ref1.iBGCLopnummer, "iBGCLopnummer");
        assertEquals("0", ref1.iAvibildmarkering, "iAvibildmarkering");
    }
}
