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
     * Normalizes a price.
     * @param price Price to normalize.
     * @return price, normalized.
     */
    private double normalizePrice(double price) {
        return (price - basisOfBB) / (upperOfBB - lowerOfBB);
    }

    /**
     * The input values to be fed into the neural network.
     * @return relevant input values for neural network use.
     */
    public double[] neuralNetworkInputs() {
        double priceNorm = normalizePrice(bar.getClosePrice().doubleValue());
        double sma20Norm = normalizePrice(sma20);
        double sma50Norm = normalizePrice(sma50);
        double sma200Norm = normalizePrice(sma200);
        double volume = bar.getVolume().doubleValue();
        return new double[] { rsi, priceNorm, sma20Norm, sma50Norm, sma200Norm, volume, macd, widthOfBB };
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
