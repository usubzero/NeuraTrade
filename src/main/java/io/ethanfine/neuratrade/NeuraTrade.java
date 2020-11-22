package io.ethanfine.neuratrade;

import io.ethanfine.neuratrade.data.models.BarDataPoint;
import io.ethanfine.neuratrade.data.models.BarDataSeries;
import io.ethanfine.neuratrade.ui.UIMain;
import io.ethanfine.neuratrade.util.CSVIO;

public class NeuraTrade {

    /**
     * Creates a new UIMain instance for the app.
     * @param args Unused
     */
    public static void main(String args[]) {
        new UIMain();
        BarDataSeries cbBarDataSeries = CSVIO.readFile("resources/BTC-USD,14400.csv");
        CSVIO.writeBarDataSeriesToFile(cbBarDataSeries, "resources/BTC-USD,14400-w.csv");
        if (cbBarDataSeries == null) {
            System.out.println("Failed to load CB Bar data series from CSV");
            return;
        }
        CSVIO.writeBarDataSeriesToFile(cbBarDataSeries, "cb.csv");
        for (int i = 0; i < cbBarDataSeries.getBarCount(); i++) {
            BarDataPoint bdpI = cbBarDataSeries.getBarDataPoint(i);
            System.out.println("time: " + bdpI.bar.getBeginTime().toEpochSecond() + ", timeEDEBUG: " + bdpI.epochDebugTODORM + ", open: " + bdpI.bar.getOpenPrice() +
                    ", high: " + bdpI.bar.getHighPrice() + ", low: " + bdpI.bar.getLowPrice() + ", close: " + bdpI.bar.getLowPrice() +
                    ",  volume: " + bdpI.bar.getVolume() + ", rsi: " + bdpI.rsi + ", sma20: " + bdpI.sma20 + ", sma50: " + bdpI.sma50 +
                    ", sma200: " + bdpI.sma200 + "\n");
//            System.out.println(Double.toString(bdpI.macd) + "\n");
        }

    }

}
