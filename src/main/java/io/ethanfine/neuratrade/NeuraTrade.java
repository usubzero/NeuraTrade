package io.ethanfine.neuratrade;

import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;
import io.ethanfine.neuratrade.ui.UIMain;
import io.ethanfine.neuratrade.util.PerformanceStats;

public class NeuraTrade {

    /**
     * Creates a new UIMain instance for the app.
     * @param args Unused
     */
    public static void main(String args[]) {
        new UIMain();
        PerformanceStats.printReturnsForRandomPeriods(CBTimeGranularity.HOUR);
    }

}
