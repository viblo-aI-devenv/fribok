package se.swedsoft.bookkeeping.calc.math;


import se.swedsoft.bookkeeping.data.SSInventory;
import se.swedsoft.bookkeeping.data.SSInventoryRow;
import se.swedsoft.bookkeeping.data.SSProduct;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * User: Andreas Lago
 * Date: 2006-sep-22
 * Time: 16:34:16
 */
public class SSInventoryMath {
    private SSInventoryMath() {}

    public static boolean inPeriod(SSInventory iInventory, LocalDate iTo) {
        LocalDate iDate = iInventory.getLocalDate();

        return iDate != null && iTo != null && !iDate.isAfter(iTo);

    }

    public static boolean inPeriod(SSInventory iInventory, LocalDate iFrom, LocalDate iTo) {
        LocalDate iDate = iInventory.getLocalDate();

        return iDate != null && iFrom != null && iTo != null
                && !iDate.isBefore(iFrom) && !iDate.isAfter(iTo);
    }

    /**
     *
     * @param iInventory
     * @param iProduct
     * @return
     */
    public static boolean hasProduct(SSInventory iInventory, SSProduct iProduct) {

        for (SSInventoryRow iRow : iInventory.getRows()) {
            if (iRow.hasProduct(iProduct)) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, Integer> getStockInfluencing(List<SSInventory> iInventories) {
        Map<String, Integer> iInventoryCount = new HashMap<>();

        for (SSInventory iInventory : iInventories) {
            for (SSInventoryRow iRow : iInventory.getRows()) {
                if (iRow.getChange() == null) {
                    continue;
                }
                Integer iReserved = iInventoryCount.get(iRow.getProductNr()) == null
                        ? iRow.getChange()
                        : iInventoryCount.get(iRow.getProductNr()) + iRow.getChange();

                iInventoryCount.put(iRow.getProductNr(), iReserved);
            }
        }
        return iInventoryCount;
    }

}
