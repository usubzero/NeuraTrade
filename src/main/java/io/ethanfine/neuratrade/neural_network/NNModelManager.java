package io.ethanfine.neuratrade.neural_network;

import io.ethanfine.neuratrade.coinbase.models.CBProduct;
import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;

public class NNModelManager {

    public static NNModelManager shared;

    public NNModel hourModel;
    public NNModel sixHourModel;

    static {
        shared = new NNModelManager();
    }

    public NNModelManager() {
        hourModel = new NNModel(CBProduct.BTCUSD, CBTimeGranularity.MINUTE_FIFTEEN);
        sixHourModel = new NNModel(CBProduct.BTCUSD, CBTimeGranularity.HOUR);
        /* Use models for time granularity one smaller than actual time granularity so that more buys/sells occur due to
        the increased volatility of greater time granularities */
    }

}
