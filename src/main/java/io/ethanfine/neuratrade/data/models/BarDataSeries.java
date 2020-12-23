package io.ethanfine.neuratrade.data.models;

import io.ethanfine.neuratrade.Config;
import io.ethanfine.neuratrade.coinbase.models.CBProduct;
import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;
import io.ethanfine.neuratrade.external_data.FNGPublicData;
import io.ethanfine.neuratrade.neural_network.NNModel;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandWidthIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.DifferenceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Map;

public class BarDataSeries {

    public CBProduct product;
    public CBTimeGranularity timeGranularity;

    private final ArrayList<BarDataPoint> barDataArray;

    // Indicators derived from the BarDataPoints in barDataArray
    private RSIIndicator rsiIndicator;
    private DifferenceIndicator macdIndicator;
    private BollingerBandsMiddleIndicator basisOfBBIndicator;
    private BollingerBandsUpperIndicator upperOfBBIndicator;
    private BollingerBandsLowerIndicator lowerOfBBIndicator;
    private BollingerBandWidthIndicator widthOfBBIndicator;
    private SMAIndicator sma20Indicator;
    private SMAIndicator sma50Indicator;
    private SMAIndicator sma200Indicator;
    private ClosePriceIndicator closePriceIndicator;
    private LowPriceIndicator lowestPriceIndicator;
    private HighPriceIndicator highPriceIndicator;

    private boolean isImported;

    public BarDataSeries(CBProduct product, BarSeries barSeries, CBTimeGranularity timeGranularity, boolean isImported) {
        this.product = product;
        this.barDataArray = new ArrayList<>();
        this.timeGranularity = timeGranularity;

        closePriceIndicator = new ClosePriceIndicator(barSeries);
        rsiIndicator = new RSIIndicator(closePriceIndicator, 14);
        MACDIndicator macdIndicator = new MACDIndicator(closePriceIndicator);
        EMAIndicator macdSignalIndicator = new EMAIndicator(macdIndicator, 9);
        this.macdIndicator = new DifferenceIndicator(macdIndicator, macdSignalIndicator);
        sma20Indicator = new SMAIndicator(closePriceIndicator, 20);
        sma50Indicator = new SMAIndicator(closePriceIndicator, 50);
        sma200Indicator = new SMAIndicator(closePriceIndicator, 200);
        StandardDeviationIndicator sd20Indicator = new StandardDeviationIndicator(closePriceIndicator, 20);
        basisOfBBIndicator = new BollingerBandsMiddleIndicator(sma20Indicator);
        lowerOfBBIndicator = new BollingerBandsLowerIndicator(basisOfBBIndicator, sd20Indicator);
        upperOfBBIndicator = new BollingerBandsUpperIndicator(basisOfBBIndicator, sd20Indicator);
        widthOfBBIndicator = new BollingerBandWidthIndicator(upperOfBBIndicator, basisOfBBIndicator, lowerOfBBIndicator);
        lowestPriceIndicator = new LowPriceIndicator(barSeries);
        highPriceIndicator = new HighPriceIndicator(barSeries);

        this.isImported = isImported;

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
            bdp.basisOfBB = basisOfBBIndicator.getValue(i).doubleValue();
            bdp.lowerOfBB = lowerOfBBIndicator.getValue(i).doubleValue();
            bdp.upperOfBB = upperOfBBIndicator.getValue(i).doubleValue();
            bdp.sma20 = sma20Indicator.getValue(i).doubleValue();
            bdp.sma50 = sma50Indicator.getValue(i).doubleValue();
            bdp.sma200 = sma200Indicator.getValue(i).doubleValue();
            bdp.widthOfBB = widthOfBBIndicator.getValue(i).doubleValue() / 100;
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

                bdpY.barActionLabeled = BarAction.HOLD;
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
                    localPeriodHighBDP.barActionLabeled = BarAction.SELL;
                    localPeriodLowBDP.barActionLabeled = BarAction.BUY;
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
                if (bdp.barActionLabeled == BarAction.HOLD) continue;
                if (bdpBefore.barActionLabeled == bdp.barActionLabeled || (lastRepeated != null && repeating && lastRepeated == bdp.barActionLabeled)) {
                    repeating = true;
                    lastRepeated = bdp.barActionLabeled;
                    if (bdp.barActionLabeled == BarAction.BUY) {
                        bdp.barActionLabeled = BarAction.HOLD;
                    } else if (bdp.barActionLabeled == BarAction.SELL) {
                        bdp.barActionLabeled = BarAction.HOLD;
                    }
                }
                if (bdpBefore.barActionLabeled != bdp.barActionLabeled && (lastRepeated != null && repeating && lastRepeated != bdp.barActionLabeled)) {
                    repeating = false;
                }
            }
        }
//        if (Config.shared.filterRepetitiveSignals) {
//            for (int i = 1; i < getBarCount(); i++) {
//                BarDataPoint bdpBefore = getBarDataPoint(i - 1);
//                BarDataPoint bdp = getBarDataPoint(i);
//                if (bdpBefore.barActionLabeled == bdp.barAction) {
//                    if (bdp.barActionLabeled == BarAction.BUY) {
//                        bdp.barActionLabeled = BarAction.HOLD;
//                    } else if (bdp.barActionLabeled == BarAction.SELL) {
//                        bdp.barActionLabeled = BarAction.HOLD;
//                    }
//                }
//            }
//        }
    }

    /**
     * Add trades to the bars of the bar data array depending on whether model would have held, sold, or bought at each
     * bar.
     * @param model The model to base predictions on.
     */
    public void labelTradePredictions(NNModel model) {
        try {
            if (model != null) {
                int lSell = -11;
                for (int i = 0; i < getBarCount(); i++) {
                    BarDataPoint bdpI = getBarDataPoint(i);
                    BarAction predictedBarAction = model.predict(bdpI.neuralNetworkInputs());
                    int llSell = lSell;
//                    if (i < 20) predictedBarAction = BarAction.HOLD;
                    // TODO: calculate avg difference between predictive and basis for a bunch of random periods
                    if (predictedBarAction == BarAction.SELL) lSell = i;
                    if (i < 20 && !isImported) predictedBarAction = BarAction.HOLD;
                    if (i - llSell <= 5 && predictedBarAction != BarAction.SELL) predictedBarAction = BarAction.HOLD;
                    // TODO: isImported can't depend on state; must depend on this bds
                    // TODO: maybe add element to chart to display that 20 first rendered bars can't display predictions if live data
                    // Calculated indicator values aren't correct for first 20 values due to them depending on previous 20 values
                    getBarDataPoint(i).tradesPredicted.add(
                            new Trade(bdpI.bar.getBeginTime().toEpochSecond() * 1000,  //  TODO: end time?
                                    bdpI.bar.getClosePrice().doubleValue(),
                                    predictedBarAction)
                    );
                    // TODO: add predictions in between for last bar and previous bars that occured with app running, not only on current price
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in refresh: " + e.getMessage());
        }
    }

    /**
     * Get all the BarDataPoints which match a certain filter.
     * @param filter The predicate that evaluates each bar data point when choosing to keep or remove it when filtering.
     * @return a list of BarDataPoints matching the predicate filter.
     */
    public ArrayList<BarDataPoint> filterDataPoints(java.util.function.Predicate<? super BarDataPoint> filter) {
        ArrayList<BarDataPoint> actionPoints = new ArrayList<>(barDataArray);
        actionPoints.removeIf(filter);
        return actionPoints;
    }

    /** The percent return that would be generated over this bar series if every BarAction were executed.
     * Precondition: BarActions have been assigned to this bar series.
     * @return The expected return of this bar series.
     */
    public double expectedPercentReturn() {
        return 0.0; // TODO
    }

    // TODO: doc
    public BarAction mostRecentBarAction() {
        int barCount = getBarCount();
        if (barCount < 1) return null;
        BarDataPoint bdpLast = getBarDataPoint(barCount - 1);
        int bdpLastTradeCount = bdpLast.tradesPredicted.size();
        if (bdpLastTradeCount < 1) return null;
        return bdpLast.tradesPredicted.get(bdpLastTradeCount - 1).barAction;
    }

    // TODO: doc
    public ArrayList<Trade> tradesForPredictedBarAction(BarAction barAction) {
        ArrayList<Trade> trades = new ArrayList<>();
        for (int i = 0; i < getBarCount(); i++) {
            BarDataPoint bdpI = getBarDataPoint(i);
            ArrayList<Trade> bdpBarActionTrades = new ArrayList<>(bdpI.tradesPredicted);
            bdpBarActionTrades.removeIf(trade -> trade.barAction != barAction);
            trades.addAll(bdpBarActionTrades);
        }
        return trades;
    }

    // TODO: doc
    public double basisReturn() {
        int minBarCount = isImported ? 2 : 22;
        if (barDataArray.size() < minBarCount) return 0;
        double open = barDataArray.get(minBarCount - 2).bar.getOpenPrice().doubleValue();
        // first 20 BDPs have inaccurate indicator values due to them depending on previous values
        double close = barDataArray.get(barDataArray.size() - 1).bar.getClosePrice().doubleValue();
        return (close - open) / open * 100;
    }

    // TODO: doc
    public double predictionsReturn() {
        if (barDataArray.size() >= 2) {
            ArrayList<Trade> buyTrades = tradesForPredictedBarAction(BarAction.BUY);
            ArrayList<Trade> sellTrades = tradesForPredictedBarAction(BarAction.SELL);
//            int buyTradesI = 0;
//            int sellTradesI = 0;
            double investmentVal = 1;
            boolean holding = false;
            Trade lBuyTrade = null;
            Trade lSellTrade = null;
            while(!buyTrades.isEmpty() || !sellTrades.isEmpty()) {
                Trade lBuyTradeF = lBuyTrade;
                Trade lSellTradeF = lSellTrade;
                if (lBuyTradeF != null) {
                    buyTrades.removeIf(t -> t.epoch < lBuyTradeF.epoch);
                    sellTrades.removeIf(t -> t.epoch < lBuyTradeF.epoch);
                }
                if (lSellTradeF != null) {
                    buyTrades.removeIf(t -> t.epoch < lSellTradeF.epoch);
                    sellTrades.removeIf(t -> t.epoch < lSellTradeF.epoch);
                }
                if (holding && lBuyTrade != null) {
                    if (sellTrades.isEmpty()) break;
                    lSellTrade = sellTrades.get(0);
                    holding = false;
                    investmentVal += (lSellTrade.price - lBuyTrade.price) / lBuyTrade.price * investmentVal;
                } else {
                    if (buyTrades.isEmpty()) break;
                    lBuyTrade = buyTrades.get(0);
                    holding = true;
                }
            }
            if (lBuyTrade != null && (lSellTrade == null || lBuyTrade.epoch > lSellTrade.epoch)) {
                double closePrice = barDataArray.get(barDataArray.size() - 1).bar.getClosePrice().doubleValue();
                investmentVal += (closePrice - lBuyTrade.price) / lBuyTrade.price * investmentVal;
            }
            return (investmentVal - 1) * 100;
        }
        return 0;
    }

}
