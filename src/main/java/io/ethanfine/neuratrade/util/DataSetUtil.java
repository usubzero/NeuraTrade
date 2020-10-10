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
    public static XYDataset createTrainingChartDataset(BarDataSeries barDataSeries) {
        XYSeries buySeries = new XYSeries(barDataSeries.product.productName + " Buys");
        XYSeries sellSeries = new XYSeries(barDataSeries.product.productName + " Sells");
        ArrayList<BarDataPoint> buyBars = barDataSeries.getDataPointsForBarAction(BarAction.BUY);
        ArrayList<BarDataPoint> sellBars = barDataSeries.getDataPointsForBarAction(BarAction.SELL);
        for (int i = 0; i < barDataSeries.getBarCount(); i++) {
            BarDataPoint bdpI = barDataSeries.getBarDataPoint(i);
            if (buyBars.contains(bdpI)) {
                buySeries.add(bdpI.bar.getBeginTime().toEpochSecond() * 1000, bdpI.bar.getLowPrice().doubleValue());
            }
            if (sellBars.contains(bdpI)) {
                sellSeries.add(bdpI.bar.getBeginTime().toEpochSecond() * 1000, bdpI.bar.getHighPrice().doubleValue());
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(buySeries);
        dataset.addSeries(sellSeries);
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
