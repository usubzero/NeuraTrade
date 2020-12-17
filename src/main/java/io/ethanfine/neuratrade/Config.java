package io.ethanfine.neuratrade;

import io.ethanfine.neuratrade.coinbase.models.CBProduct;
import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;
import io.ethanfine.neuratrade.data.models.BarDataSeries;
import io.ethanfine.neuratrade.ui.models.ChartBarCount;

public class Config {

    public static Config shared;

    /**
     * Trade parameters
     */
    public CBProduct product = CBProduct.BTCUSD;
    public CBTimeGranularity timeGranularity = CBTimeGranularity.DAY;
    public ChartBarCount chartBarCount = ChartBarCount.HUNDRED_TWO;

    /**
     * Training data generation parameters.
     * BuySellMinVolatilities should be interpreted as percentages.
     */
    public double minsTimeGranularityBuySellMinVolatility = 0.1;
    public double minsFiveTimeGranularityBuySellMinVolatility = 0.3;
    public double minsFifteenTimeGranularityBuySellMinVolatility = 2;
    public double hourTimeGranularityBuySellMinVolatility = 4;
    public double hourSixTimeGranularityBuySellMinVolatility = 6;
    public double dayTimeGranularityBuySellMinVolatility = 7;

    /**
     * Whether or not to remove repetitive buy or sell signals
     */
    public boolean filterRepetitiveSignals = false;

    /**
     * API parameters
     */
//    public int tickerUpdatesPerSecond = 5;

    static {
        shared = new Config();
    }

}
