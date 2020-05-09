package io.ethanfine.neuratrade.data.models;

import org.ta4j.core.Bar;

public class BarDataPoint {

    public Bar bar;
    private BarDataSeries barDataSeries;

    public double rsi;
    public double macd;

    public BarAction barAction;

    public BarDataPoint(Bar bar, BarDataSeries barDataSeries) {
        this.bar = bar;
        this.barDataSeries = barDataSeries;
    }

    public double volatility() {
        return ((bar.getHighPrice().doubleValue() - bar.getLowPrice().doubleValue()) / bar.getLowPrice().doubleValue()) * 100;
    }

}
