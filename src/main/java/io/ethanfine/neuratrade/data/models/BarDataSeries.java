package io.ethanfine.neuratrade.data.models;

import io.ethanfine.neuratrade.Config;
import io.ethanfine.neuratrade.coinbase.models.CBProduct;
import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;
import io.ethanfine.neuratrade.external_data.FNGPublicData;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;

import java.util.ArrayList;
import java.util.Map;

public class BarDataSeries {

    public CBProduct product;
    CBTimeGranularity timeGranularity;

    private final ArrayList<BarDataPoint> barDataArray;

    // Indicators derived from the BarDataPoints in barDataArray
    public RSIIndicator rsiIndicator;
    public MACDIndicator macdIndicator;
    public ClosePriceIndicator closePriceIndicator;
    public LowPriceIndicator lowestPriceIndicator;
    public HighPriceIndicator highPriceIndicator;

    public BarDataSeries(CBProduct product, BarSeries barSeries, CBTimeGranularity timeGranularity) {
        this.product = product;
        this.barDataArray = new ArrayList<>();
        this.timeGranularity = timeGranularity;

        closePriceIndicator = new ClosePriceIndicator(barSeries);
        rsiIndicator = new RSIIndicator(closePriceIndicator, Config.shared.rsiCalculationTickCount);
        macdIndicator = new MACDIndicator(closePriceIndicator);
        lowestPriceIndicator = new LowPriceIndicator(barSeries);
        highPriceIndicator = new HighPriceIndicator(barSeries);

        mapBarsToBarDataPoints(barSeries);
        labelBarActions();
        assignFearAndGreedIndexValues();
    }

    /**
     * Populate the BarDataSeries with BarDataPoints created from each of the bars in barSeries. Each BarDataPoint is
     * assigned an RSI and MACD value based on the indicator values calculated for barSeries.
     * @param barSeries the BarSeries that this BarDataSeries is based on.
     */
    private void mapBarsToBarDataPoints(BarSeries barSeries) {

        for (int i = 0; i < barSeries.getBarCount(); i++) {
            BarDataPoint bdp = new BarDataPoint(barSeries.getBar(i), this); // TODO: watch out for strong reference cycles
            bdp.rsi = rsiIndicator.getValue(i).doubleValue();
            bdp.macd = macdIndicator.getValue(i).doubleValue();
            barDataArray.add(bdp);
        }
    }

    /**
     * If the timeGranularity is of value DAY, then fear and greed index values are assigned to each BarDataPoint in
     * the series.
     */
    private void assignFearAndGreedIndexValues() {
        // Fear and greed data is only really relevant for higher time-frames
        if (timeGranularity == CBTimeGranularity.DAY) {
            int fngDataPointCount = 830;
            Map<Long, Integer> fngDataPoints = FNGPublicData.getFNGIndexDataPoints(fngDataPointCount);
            if (fngDataPoints.size() < fngDataPointCount) {
                System.out.println("Could not assign fear and greed index values to day bar data series.");
                return;
            }

            Integer[] fngDataPointValues = fngDataPoints.values().toArray(new Integer[fngDataPoints.size()]);

            int bdpI = 0;
            int startI = (fngDataPoints.size() > barDataArray.size()) ? fngDataPoints.size() - barDataArray.size() : 0;
            for (int i = startI; i < fngDataPoints.size(); i++) {
                if (barDataArray.size() == bdpI) {
                    break;
                }
                barDataArray.get(bdpI).fngIndex = fngDataPointValues[i];
                if (i >= fngDataPointCount - barDataArray.size() - 1) {
                    bdpI++;
                }
            }
        }
    }

    /**
     * Get the number of bars in the series.
     * @return number of bars in series.
     */
    public int getBarCount() {
        return barDataArray.size();
    }

    /**
     * Get the bar data point with index i in the series.
     * @param i index to find data point.
     * @return bar data point i
     */
    public BarDataPoint getBarDataPoint(int i) {
        return barDataArray.get(i);
    }

//    public BarDataPoint getBarDataPointWithLowestLow() {
//        BarDataPoint lowestLow = null;
//        for (int i = 0; i < barDataArray.size(); i++) {
//            BarDataPoint bdp = getBarDataPoint(i);
//            if (lowestLow == null) {
//                lowestLow = bdp;
//            } else {
//                if (lowestPriceIndicator.getValue(i).doubleValue() < lowestLow.bar.getLowPrice().doubleValue()) {
//                    lowestLow = bdp;
//                }
//            }
//        }
//        return lowestLow;
//    }
//
//    public BarDataPoint getBarDataPointWithHighestHigh() {
//        BarDataPoint highestHigh = null;
//        for (int i = 0; i < barDataArray.size(); i++) {
//            BarDataPoint bdp = getBarDataPoint(i);
//            if (highestHigh == null) {
//                highestHigh = bdp;
//            } else {
//                if (lowestPriceIndicator.getValue(i).doubleValue() > highestHigh.bar.getLowPrice().doubleValue()) {
//                    highestHigh = bdp;
//                }
//            }
//        }
//        return highestHigh;
//    }

    /**
     * Label training data based on the BarDataPoints associated with the series. This algorithm assigns some
     * BarDataPoints in barDataArray a BUY or SELL BarAction. The minimum percent difference between a buy and sell
     * action is as defined as for the buySellMinVolatility of the timeGranularity defined in Config. There should be
     * no successive buy signals and no successive sell signals.
     */
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
                    if (localPeriodHighBDP == null ||
                            localPeriodBDP.bar.getHighPrice().doubleValue() >
                                    localPeriodHighBDP.bar.getHighPrice().doubleValue()) {
                        localPeriodHighBDP = localPeriodBDP;
                    }
                    if (localPeriodLowBDP == null ||
                            localPeriodBDP.bar.getLowPrice().doubleValue() <
                                    localPeriodLowBDP.bar.getLowPrice().doubleValue()) {
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

    /**
     * Get all the BarDataPoints which have a BarAction = barAction.
     * @param barAction the BarAction to find BarDataPoints for.
     * @return a list of BarDataPoints with barAction as their BarAction.
     */
    public ArrayList<BarDataPoint> getDataPointsForBarAction(BarAction barAction) {
        ArrayList<BarDataPoint> actionPoints = new ArrayList<>(barDataArray);
        actionPoints.removeIf(bdp -> !(bdp.barAction == barAction));
        return actionPoints;
    }

    /** The percent return that would be generated over this bar series if every BarAction were executed.
     * Precondition: BarActions have been assigned to this bar series.
     * @return The expected return of this bar series.
     */
    public double expectedPercentReturn() {
        return 0.0; // TODO
    }

}
