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

        labelBarActions();
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

    private void labelBarActions() {
        double volatilityThreshold = Config.shared.timeGranularity.buySellMinVolatility();
        ArrayList<BarDataPoint> bdpsInLocalPeriod = new ArrayList<>();
        int nextPeriodI = -1;
        for (int i = 0; i < getBarCount(); i++) {
            if (i < nextPeriodI) {
                continue;
            }
            for (int y = i; y < getBarCount(); y++) {
                BarDataPoint bdpY = getBarDataPoint(y);
                bdpsInLocalPeriod.add(bdpY);

                BarDataPoint localPeriodHighBDP = null;
                BarDataPoint localPeriodLowBDP = null;
                for (BarDataPoint localPeriodBDP : bdpsInLocalPeriod) {
                    if (localPeriodHighBDP == null || localPeriodBDP.bar.getHighPrice().doubleValue() > localPeriodHighBDP.bar.getHighPrice().doubleValue()) {
                        localPeriodHighBDP = localPeriodBDP;
                    }
                    if (localPeriodLowBDP == null || localPeriodBDP.bar.getLowPrice().doubleValue() < localPeriodLowBDP.bar.getLowPrice().doubleValue()) {
                        localPeriodLowBDP = localPeriodBDP;
                    }
                }

                double localPeriodVolatility = ((localPeriodHighBDP.bar.getHighPrice().doubleValue() - localPeriodLowBDP.bar.getLowPrice().doubleValue()) / localPeriodLowBDP.bar.getLowPrice().doubleValue()) * 100;

                if (y + 1 < getBarCount() &&
                        (bdpY.bar.isBullish() && getBarDataPoint(y + 1).bar.isBullish() ||
                                bdpY.bar.isBearish() && getBarDataPoint(y + 1).bar.isBearish())) continue; // if continuing trend short term, expand local period

                if (localPeriodVolatility >= volatilityThreshold) {
                    localPeriodHighBDP.barAction = BarAction.SELL;
                    localPeriodLowBDP.barAction = BarAction.BUY;
                    bdpsInLocalPeriod.clear();
                    nextPeriodI = y + 1;
                }
            }
        }

        BarAction lastRepeated = null;
        boolean repeating = false;
        if (Config.shared.filterRepetitiveSignals) {
            for (int i = 1; i < getBarCount(); i++) {
                BarDataPoint bdpBefore = getBarDataPoint(i - 1);
                BarDataPoint bdp = getBarDataPoint(i);
                if (bdp.barAction == BarAction.HOLD) continue;
                if (bdpBefore.barAction == bdp.barAction || (lastRepeated != null && repeating && lastRepeated == bdp.barAction)) {
                    repeating = true;
                    lastRepeated = bdp.barAction;
                    if (bdp.barAction == BarAction.BUY) {
                        bdp.barAction = BarAction.HOLD;
                    } else if (bdp.barAction == BarAction.SELL) {
                        bdp.barAction = BarAction.HOLD;
                    }
                }
                if (bdpBefore.barAction != bdp.barAction && (lastRepeated != null && repeating && lastRepeated != bdp.barAction)) {
                    repeating = false;
                }
            }
        }
//        if (Config.shared.filterRepetitiveSignals) {
//            for (int i = 1; i < getBarCount(); i++) {
//                BarDataPoint bdpBefore = getBarDataPoint(i - 1);
//                BarDataPoint bdp = getBarDataPoint(i);
//                if (bdpBefore.barAction == bdp.barAction) {
//                    if (bdp.barAction == BarAction.BUY) {
//                        bdp.barAction = BarAction.HOLD;
//                    } else if (bdp.barAction == BarAction.SELL) {
//                        bdp.barAction = BarAction.HOLD;
//                    }
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
