package io.ethanfine.neuratrade;

import io.ethanfine.neuratrade.coinbase.CBProduct;

public class Config {

    public static Config shared;

    /*
    Trade parameters
     */
    public CBProduct product;
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
    }

}
