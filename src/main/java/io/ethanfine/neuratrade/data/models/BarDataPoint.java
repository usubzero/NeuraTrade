package io.ethanfine.neuratrade.data.models;

import org.ta4j.core.Bar;

public class BarDataPoint {

    /**
     * The Bar representation of this data point.
     */
    public Bar bar;
    /**
     * The bar data series that his data point belongs to.
     */
    private BarDataSeries barDataSeries;

    /**
     * Indicator values for this data point.
     */
    public double rsi;
    public double macd;
    public double basisOfBB;
    public double upperOfBB;
    public double lowerOfBB;
    public double widthOfBB;
    public double sma20;
    public double sma50;
    public double sma200;
    public double fngIndex;
    public double epochDebugTODORM;

    /**
     * The labeled action associated with this data point.
     */
    public BarAction barActionLabeled;
    /**
     * The predicted action associated with this data point.
     */
    public BarAction barActionPredicted;

    public BarDataPoint(Bar bar, BarDataSeries barDataSeries) {
        this.bar = bar;
        this.barDataSeries = barDataSeries;
    }

    /**
     * Calculates the percent difference between the highest price and lowest price within this data point's interval.
     * @return Volatility during this data point's interval.
     */
    public double volatility() {
        return ((bar.getHighPrice().doubleValue() - bar.getLowPrice().doubleValue())
                / bar.getLowPrice().doubleValue()) * 100;
    }

}
