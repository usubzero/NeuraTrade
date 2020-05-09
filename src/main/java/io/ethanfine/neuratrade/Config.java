package io.ethanfine.neuratrade;

import io.ethanfine.neuratrade.coinbase.models.CBProduct;
import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;
import io.ethanfine.neuratrade.ui.models.ChartBarCount;

public class Config {

    public static Config shared;

    /*
    Trade parameters
     */
    public CBProduct product = CBProduct.BTCUSD;
    public CBTimeGranularity timeGranularity = CBTimeGranularity.HOUR;
    public ChartBarCount chartBarCount = ChartBarCount.HUNDRED_TWO;
    public int rsiCalculationTickCount = 14;

    /*
    Training data generation parameters
     */
    public double minsTimeGranularityBuySellMinVolatility = 1;
    public double minsFiveTimeGranularityBuySellMinVolatility = 1;
    public double minsFifteenTimeGranularityBuySellMinVolatility = 2;
    public double hourTimeGranularityBuySellMinVolatility = 4;
    public double hourSixTimeGranularityBuySellMinVolatility = 6;
    public double dayTimeGranularityBuySellMinVolatility = 7;

    // Whether or not to remove repetitive buy or sell signals
    public boolean filterRepetitiveSignals = false;

    /*
    API parameters
     */
//    public int tickerUpdatesPerSecond = 5;

    static {
        shared = new Config();
    }

}
