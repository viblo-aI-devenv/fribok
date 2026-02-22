package se.swedsoft.bookkeeping.data.system;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import se.swedsoft.bookkeeping.data.SSInvoice;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link SSDB} invoice CRUD operations.
 *
 * <p>{@code addInvoice} automatically assigns the invoice number, so after
 * calling it the number is available via {@code invoice.getNumber()}.  Each
 * test records that number and deletes the invoice in a {@code finally} block
 * to keep the DB clean.</p>
 */
@Tag("integration")
class SSInvoiceIntegrationTest {

    @BeforeAll
    static void openDatabase() throws Exception {
        SSDBTestFixture.setupOnce();
    }

    @BeforeEach
    void clearCaches() {
        SSDBTestFixture.resetCaches();
    }

    // ---- addInvoice / getInvoices ----

    @Test
    void addedInvoiceAppearsInGetInvoices() {
        SSInvoice inv = invoice("INV-IT-CUST-001", "Invoice IT Customer");
        SSDB.getInstance().addInvoice(inv);

        try {
            SSDBTestFixture.resetCaches();
            List<SSInvoice> all = SSDB.getInstance().getInvoices();

            assertThat(all).extracting(SSInvoice::getNumber).contains(inv.getNumber());
        } finally {
            SSDB.getInstance().deleteInvoice(inv);
        }
    }

    @Test
    void addedInvoiceNumberIsPositive() {
        SSInvoice inv = invoice("INV-IT-CUST-002", "Invoice IT Customer 2");
        SSDB.getInstance().addInvoice(inv);

        try {
            assertThat(inv.getNumber()).isGreaterThan(0);
        } finally {
            SSDB.getInstance().deleteInvoice(inv);
        }
    }

    @Test
    void addedInvoiceCustomerNrRoundTrips() {
        SSInvoice inv = invoice("INV-IT-CUST-003", "Round-Trip Customer");
        SSDB.getInstance().addInvoice(inv);

        try {
            SSDBTestFixture.resetCaches();
            SSInvoice fetched = SSDB.getInstance().getInvoice(inv);

            assertThat(fetched).isNotNull();
            assertThat(fetched.getCustomerNr()).isEqualTo("INV-IT-CUST-003");
        } finally {
            SSDB.getInstance().deleteInvoice(inv);
        }
    }

    @Test
    void addedInvoiceCustomerNameRoundTrips() {
        SSInvoice inv = invoice("INV-IT-CUST-004", "Name Round-Trip AB");
        SSDB.getInstance().addInvoice(inv);

        try {
            SSDBTestFixture.resetCaches();
            SSInvoice fetched = SSDB.getInstance().getInvoice(inv);

            assertThat(fetched).isNotNull();
            assertThat(fetched.getCustomerName()).isEqualTo("Name Round-Trip AB");
        } finally {
            SSDB.getInstance().deleteInvoice(inv);
        }
    }

    @Test
    void addedInvoiceDateRoundTrips() {
        Date date = new Date(1_700_000_000_000L);
        SSInvoice inv = invoice("INV-IT-CUST-005", "Date Customer");
        inv.setDate(date);
        SSDB.getInstance().addInvoice(inv);

        try {
            SSDBTestFixture.resetCaches();
            SSInvoice fetched = SSDB.getInstance().getInvoice(inv);

            assertThat(fetched).isNotNull();
            assertThat(fetched.getDate()).isEqualTo(date);
        } finally {
            SSDB.getInstance().deleteInvoice(inv);
        }
    }

    @Test
    void addedInvoiceCurrencyRateRoundTrips() {
        SSInvoice inv = invoice("INV-IT-CUST-006", "Currency Customer");
        inv.setCurrencyRate(new BigDecimal("10.50"));
        SSDB.getInstance().addInvoice(inv);

        try {
            SSDBTestFixture.resetCaches();
            SSInvoice fetched = SSDB.getInstance().getInvoice(inv);

            assertThat(fetched).isNotNull();
            assertThat(fetched.getCurrencyRate()).isEqualByComparingTo(new BigDecimal("10.50"));
        } finally {
            SSDB.getInstance().deleteInvoice(inv);
        }
    }

    // ---- deleteInvoice ----

    @Test
    void deletedInvoiceDisappearsFromGetInvoices() {
        SSInvoice inv = invoice("INV-IT-DEL-001", "Delete Me Customer");
        SSDB.getInstance().addInvoice(inv);
        Integer number = inv.getNumber();
        SSDB.getInstance().deleteInvoice(inv);

        SSDBTestFixture.resetCaches();
        List<SSInvoice> all = SSDB.getInstance().getInvoices();

        assertThat(all).extracting(SSInvoice::getNumber).doesNotContain(number);
    }

    @Test
    void deletedInvoiceNotFoundByObject() {
        SSInvoice inv = invoice("INV-IT-DEL-002", "Also Delete Me");
        SSDB.getInstance().addInvoice(inv);
        SSDB.getInstance().deleteInvoice(inv);

        SSDBTestFixture.resetCaches();
        SSInvoice fetched = SSDB.getInstance().getInvoice(inv);

        assertThat(fetched).isNull();
    }

    // ---- updateInvoice ----

    @Test
    void updatedInvoiceCustomerNameRoundTrips() {
        SSInvoice inv = invoice("INV-IT-UPD-001", "Before Update");
        SSDB.getInstance().addInvoice(inv);

        try {
            SSDBTestFixture.resetCaches();
            SSInvoice fetched = SSDB.getInstance().getInvoice(inv);
            assertThat(fetched).isNotNull();

            fetched.setCustomerName("After Update");
            SSDB.getInstance().updateInvoice(fetched);

            SSDBTestFixture.resetCaches();
            SSInvoice updated = SSDB.getInstance().getInvoice(inv);

            assertThat(updated).isNotNull();
            assertThat(updated.getCustomerName()).isEqualTo("After Update");
        } finally {
            SSDB.getInstance().deleteInvoice(inv);
        }
    }

    // ---- getInvoice returns null for unknown ----

    @Test
    void getInvoiceReturnsNullForNonExistentNumber() {
        SSInvoice ghost = new SSInvoice();
        ghost.setNumber(Integer.MAX_VALUE);

        SSInvoice fetched = SSDB.getInstance().getInvoice(ghost);

        assertThat(fetched).isNull();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a minimal {@link SSInvoice} using the default constructor (which
     * does not call SSDB) and wires the customer number and name directly.
     */
    private static SSInvoice invoice(String customerNr, String customerName) {
        SSInvoice inv = new SSInvoice();
        inv.setCustomerNr(customerNr);
        inv.setCustomerName(customerName);
        inv.setDate(new Date());
        inv.setCurrencyRate(new BigDecimal("1"));
        return inv;
    }
}
