package io.ethanfine.neuratrade;

import io.ethanfine.neuratrade.coinbase.CBProduct;
import io.ethanfine.neuratrade.coinbase.CBTimeGranularity;

public class Config {

    public static Config shared;

    /*
    Trade parameters
     */
    public CBProduct product;
    public CBTimeGranularity timeGranularity;
    public int rsiCalculationTickCount = 14;

    /*
    API parameters
     */
    public int tickerUpdatesPerSecond = 5;

    static {
        shared = new Config();
    }

    private Config() {
        product = CBProduct.BTCUSD;
        timeGranularity = CBTimeGranularity.HOUR;
    }

}
