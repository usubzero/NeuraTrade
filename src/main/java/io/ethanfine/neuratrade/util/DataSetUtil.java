package io.ethanfine.neuratrade.util;

import io.ethanfine.neuratrade.Config;
import io.ethanfine.neuratrade.data.models.BarAction;
import io.ethanfine.neuratrade.data.models.BarDataPoint;
import io.ethanfine.neuratrade.data.models.BarDataSeries;
import org.jfree.data.xy.*;

import java.sql.Date;
import java.util.ArrayList;

public class DataSetUtil {

    /**
     * Create an XY-Dataset for price data. This dataset contains time on the X axis and
     * (open, high, low, close, and volume) on the Y axis for every bar in barDataSeries.
     * @param barDataSeries the BarDataSeries to base the dataset on.
     * @return The XY-Dataset as an AbstractXYDataset.
     */
    public static AbstractXYDataset createPriceDataSet(BarDataSeries barDataSeries) {
        OHLCDataItem[] data  = new OHLCDataItem[barDataSeries.getBarCount()];
        for (int i = 0; i < barDataSeries.getBarCount(); i++) {
            BarDataPoint bdpI = barDataSeries.getBarDataPoint(i);
            data[i] = new OHLCDataItem(Date.from(bdpI.bar.getBeginTime().toInstant()),
                    bdpI.bar.getOpenPrice().doubleValue(),
                    bdpI.bar.getHighPrice().doubleValue(),
                    bdpI.bar.getLowPrice().doubleValue(),
                    bdpI.bar.getClosePrice().doubleValue(),
                    bdpI.bar.getVolume().doubleValue()
            );
        }

        return new DefaultOHLCDataset(Config.shared.product + " Prices", data);
    }

    /**
     * Create an XY-Dataset with a buy series and a sell series. The buy series is based on the BarDataPoints where
     * barDataSeries would buy the product corresponding to barDataSeries. The sell series is the same as the buy series
     * but for the sell BarAction.  The points in the buy and sell series contain time on the X axis and a price to
     * buy or sell at on the Y axis.
     * @param barDataSeries the BarDataSeries to base the dataset on.
     * @return The XY-Dataset containing a buy series and a sell series as an XYDataSet.
     */
    public static XYDataset createBarActionDataset(BarDataSeries barDataSeries) {
        XYSeries lBuySeries = new XYSeries(barDataSeries.product.productName + " Labeled Buys");
        XYSeries lSellSeries = new XYSeries(barDataSeries.product.productName + " Labeled Sells");
        XYSeries pBuySeries = new XYSeries(barDataSeries.product.productName + " Predicted Buys");
        XYSeries pSellSeries = new XYSeries(barDataSeries.product.productName + " Predicted Sells");
        ArrayList<BarDataPoint> lBuyBars = barDataSeries.filterDataPoints(bdp -> !(bdp.barActionLabeled == BarAction.BUY));
        ArrayList<BarDataPoint> lSellBars = barDataSeries.filterDataPoints(bdp -> !(bdp.barActionLabeled == BarAction.SELL));
        ArrayList<BarDataPoint> pBuyBars = barDataSeries.filterDataPoints(bdp -> !(bdp.barActionPredicted == BarAction.BUY));
        ArrayList<BarDataPoint> pSellBars = barDataSeries.filterDataPoints(bdp -> !(bdp.barActionPredicted == BarAction.SELL));
        for (int i = 0; i < barDataSeries.getBarCount(); i++) {
            BarDataPoint bdpI = barDataSeries.getBarDataPoint(i);
            if (lBuyBars.contains(bdpI))
                lBuySeries.add(bdpI.bar.getBeginTime().toEpochSecond() * 1000, bdpI.bar.getLowPrice().doubleValue());
            if (lSellBars.contains(bdpI))
                lSellSeries.add(bdpI.bar.getBeginTime().toEpochSecond() * 1000, bdpI.bar.getHighPrice().doubleValue());
            if (pBuyBars.contains(bdpI))
                pBuySeries.add(bdpI.bar.getBeginTime().toEpochSecond() * 1000, bdpI.bar.getLowPrice().doubleValue());
            if (pSellBars.contains(bdpI))
                pSellSeries.add(bdpI.bar.getBeginTime().toEpochSecond() * 1000, bdpI.bar.getHighPrice().doubleValue());
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(lBuySeries);
        dataset.addSeries(lSellSeries);
        dataset.addSeries(pBuySeries);
        dataset.addSeries(pSellSeries);
        return dataset;
    }

    /**
     * Create an XY-Dataset with a fear and greed series. The fear and greed series is composed of hte fear and greed
     * index value for every bar in barDataSeries. The points in the fear and greed series contain time on the X axis
     * and the fear and greed index values on the Y axis.
     * @param barDataSeries the BarDataSeries to base the dataset on.
     * @return The XY-Dataset containing a fear and greed index series as an XYDataSet.
     */
    public static XYDataset createFNGDataset(BarDataSeries barDataSeries) {
        XYSeries fngPoints = new XYSeries(barDataSeries.product.productName + " FNG Index");
        for (int i = 0; i < barDataSeries.getBarCount(); i++) {
            BarDataPoint bdpI = barDataSeries.getBarDataPoint(i);
            fngPoints.add(bdpI.bar.getBeginTime().toEpochSecond() * 1000, bdpI.fngIndex);
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(fngPoints);
        return dataset;
    }

}
