package se.swedsoft.bookkeeping.calc.math;


import se.swedsoft.bookkeeping.data.SSIndelivery;
import se.swedsoft.bookkeeping.data.SSIndeliveryRow;
import se.swedsoft.bookkeeping.data.SSProduct;
import se.swedsoft.bookkeeping.util.SSDateUtil;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * User: Andreas Lago
 * Date: 2006-sep-22
 * Time: 16:34:16
 */
public class SSIndeliveryMath {
    private SSIndeliveryMath() {}

    /**
     *
     * @param iInventory
     * @param pTo
     * @return
     */
    public static boolean inPeriod(SSIndelivery iInventory, Date pTo) {
        return inPeriod(iInventory, SSDateUtil.toLocalDate(pTo));
    }

    public static boolean inPeriod(SSIndelivery iInventory, LocalDate pTo) {
        LocalDate iDate = iInventory.getLocalDate();

        return iDate != null && pTo != null && !iDate.isAfter(pTo);

    }

    /**
     *
     * @param iInventory
     * @param pFrom
     * @param pTo
     * @return
     */
    public static boolean inPeriod(SSIndelivery iInventory, Date pFrom, Date pTo) {
        return inPeriod(iInventory, SSDateUtil.toLocalDate(pFrom), SSDateUtil.toLocalDate(pTo));
    }

    public static boolean inPeriod(SSIndelivery iInventory, LocalDate pFrom, LocalDate pTo) {
        LocalDate iDate = iInventory.getLocalDate();

        return iDate != null && pFrom != null && pTo != null
                && !iDate.isBefore(pFrom) && !iDate.isAfter(pTo);
    }

    /**
     *
     * @param iIndelivery
     * @return
     */
    public static Integer getTotalCount(SSIndelivery iIndelivery) {
        Integer iCount = 0;

        for (SSIndeliveryRow iRow : iIndelivery.getRows()) {
            if (iRow.getChange() != null) {
                iCount = iCount + iRow.getChange();
            }

        }
        return iCount;
    }

    /**
     *
     * @param iIndelivery
     * @param iProduct
     * @return
     */
    public static boolean hasProduct(SSIndelivery iIndelivery, SSProduct iProduct) {

        for (SSIndeliveryRow iRow : iIndelivery.getRows()) {
            if (iRow.hasProduct(iProduct)) {
                return true;
            }
        }
        return false;
    }

    public static Map<String, Integer> getStockInfluencing(List<SSIndelivery> iIndeliveries) {
        Map<String, Integer> iIndeliveryCount = new HashMap<>();

        for (SSIndelivery iIndelivery : iIndeliveries) {
            for (SSIndeliveryRow iRow : iIndelivery.getRows()) {
                if (iRow.getChange() == null) {
                    continue;
                }
                Integer iReserved = iIndeliveryCount.get(iRow.getProductNr()) == null
                        ? iRow.getChange()
                        : iIndeliveryCount.get(iRow.getProductNr()) + iRow.getChange();

                iIndeliveryCount.put(iRow.getProductNr(), iReserved);
            }
        }
        return iIndeliveryCount;
    }

}
