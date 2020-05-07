package io.ethanfine.neuratrade.coinbase;

public enum CBProduct {

    BTCUSD("BTC-USD");

    public final String productName;

    private CBProduct(String productName) {
        this.productName = productName;
    }

}
