package io.ethanfine.neuratrade.data.models;

import io.ethanfine.neuratrade.Config;
import io.ethanfine.neuratrade.coinbase.CBProduct;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class BarDataSeries {

    public CBProduct product;

    private ArrayList<BarDataPoint> barDataArray;
    private BarSeries barSeries;

    // Indicators for series
    public RSIIndicator rsiIndicator;
    public MACDIndicator macdIndicator;
    public ClosePriceIndicator closePriceIndicator;
    public LowPriceIndicator lowestPriceIndicator;
    public HighPriceIndicator highPriceIndicator;

    public BarDataSeries(CBProduct product, BarSeries barSeries) {
        this.product = product;
        this.barSeries = barSeries;
        this.barDataArray = new ArrayList<>();

        closePriceIndicator = new ClosePriceIndicator(barSeries);
        rsiIndicator = new RSIIndicator(closePriceIndicator, Config.shared.rsiCalculationTickCount);
        macdIndicator = new MACDIndicator(closePriceIndicator);
        lowestPriceIndicator = new LowPriceIndicator(barSeries);
        highPriceIndicator = new HighPriceIndicator(barSeries);

        for (int i = 0; i < barSeries.getBarCount(); i++) {
            BarDataPoint bdp = new BarDataPoint(barSeries.getBar(i), this); // TODO: watch out for strong reference cycles
            bdp.rsi = rsiIndicator.getValue(i).doubleValue();
            bdp.macd = macdIndicator.getValue(i).doubleValue(); // TODO: MACD isn't giving the macd values we want
            barDataArray.add(bdp);
        }
    }

    public int getBarCount() {
        return barDataArray.size();
    }

    public BarDataPoint getBarDataPoint(int i) {
        return barDataArray.get(i);
    }

    public BarDataPoint getBarDataPointWithLowestLow() {
        BarDataPoint lowestLow = null;
        for (int i = 0; i < barDataArray.size(); i++) {
            BarDataPoint bdp = getBarDataPoint(i);
            if (lowestLow == null) {
                lowestLow = bdp;
            } else {
                if (lowestPriceIndicator.getValue(i).doubleValue() < lowestLow.bar.getLowPrice().doubleValue()) {
                    lowestLow = bdp;
                }
            }
        }
        return lowestLow;
    }

    public BarDataPoint getBarDataPointWithHighestHigh() {
        BarDataPoint highestHigh = null;
        for (int i = 0; i < barDataArray.size(); i++) {
            BarDataPoint bdp = getBarDataPoint(i);
            if (highestHigh == null) {
                highestHigh = bdp;
            } else {
                if (lowestPriceIndicator.getValue(i).doubleValue() > highestHigh.bar.getLowPrice().doubleValue()) {
                    highestHigh = bdp;
                }
            }
        }
        return highestHigh;
    }

    public void labelBarActions(double percentGainThreshold, double percentLossThreshold) { // TODO: base thresholds on granularity and volatility
        for (int i = 0; i < getBarCount(); i++) {
            BarDataPoint bdpI = getBarDataPoint(i);
            for (int y = 10; y > 0; y--) {
                if (i + y >= getBarCount()) {
                    continue;
                }
                Bar barY = barSeries.getBar(i + y);
                if (barY.getHighPrice().doubleValue() > bdpI.bar.getLowPrice().doubleValue()) { // Bullish
                    double potentialGain = ((barY.getHighPrice().doubleValue() - bdpI.bar.getLowPrice().doubleValue()) / bdpI.bar.getLowPrice().doubleValue()) * 100;
                    if (potentialGain >= percentGainThreshold) {
                        bdpI.barAction = BarAction.BUY;
                    }
                } else { // Bearish
                    double potentialLoss = Math.abs(((barY.getLowPrice().doubleValue() - bdpI.bar.getHighPrice().doubleValue())) / bdpI.bar.getHighPrice().doubleValue()) * 100;
                    if (potentialLoss >= percentLossThreshold) {
                        bdpI.barAction = BarAction.SELL;
                    }
                }
            }
        }

        // Below is less greedy than new implementation beneath
        for (int i = 1; i < getBarCount(); i++) {
            BarDataPoint bdpBefore = getBarDataPoint(i - 1);
            BarDataPoint bdp = getBarDataPoint(i);
            if (bdpBefore.barAction == bdp.barAction) {
                if (bdp.barAction == BarAction.BUY) {
                    bdp.barAction = BarAction.HOLD;
                } else if (bdp.barAction == BarAction.SELL) {
                    bdp.barAction = BarAction.HOLD;
                }
            }
        }

//        int nextActionI = -1;
//        for (int i = 0; i < getBarCount(); i++) {
//            BarDataPoint bdpI = getBarDataPoint(i);
//            if (bdpI.barAction == BarAction.HOLD || i < nextActionI) continue;
//
//            BarAction iAction = bdpI.barAction;
//            BarAction nextAction = (iAction == BarAction.BUY) ? BarAction.SELL : BarAction.BUY;
////            int nextActionI = -1;
//            BarDataPoint greatestProfitPoint = bdpI;
//            for (int y = i + 1; y < getBarCount(); y++) {
//                BarDataPoint bdpY = getBarDataPoint(y);
//                if (bdpY.barAction == BarAction.HOLD) continue;
//                if (bdpY.barAction == nextAction) {
//                    nextActionI = y;
//                    break;
//                }
//
//                double greatestProfitPointHigh = greatestProfitPoint.bar.getHighPrice().doubleValue();
//                double greatestProfitPointLow = greatestProfitPoint.bar.getLowPrice().doubleValue();
//                if (iAction == BarAction.BUY && bdpY.bar.getLowPrice().doubleValue() < greatestProfitPointLow) {
//                    greatestProfitPoint = bdpY;
//                } else if (nextAction == BarAction.SELL && bdpY.bar.getHighPrice().doubleValue() > greatestProfitPointHigh) {
//                    greatestProfitPoint = bdpY;
//                }
//            }
//
//            if (nextActionI == -1) continue;
//
//            for (int y = i; y < nextActionI; y++) {
//                BarDataPoint bdpY = getBarDataPoint(y);
//                if (bdpY.barAction == iAction && bdpY != greatestProfitPoint) {
//                    bdpY.barAction = BarAction.HOLD;
//                }
//            }
//        }
    }

    public ArrayList<BarDataPoint> getDataPointsForBarAction(BarAction barAction) {
        ArrayList<BarDataPoint> actionPoints = new ArrayList<>(barDataArray);
        actionPoints.removeIf(bdp -> !(bdp.barAction == barAction));
        return actionPoints;
    }

    /*
    The percent return that would be generated over this bar series if every bar action were taken
    Precondition: Bar actions have been assigned to this bar series
     */
    public double expectedPercentReturn() {
        return 0.0; // TODO
    }

}
