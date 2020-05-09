package io.ethanfine.neuratrade.coinbase.models;

import java.util.HashMap;

public enum CBProduct {

    BTCUSD("BTC-USD"),
    ETHUSD("ETH-USD"),
    BCHUSD("BCH-USD"),
    LTCUSD("LTC-USD"),
    LINKUSD("LINK-USD"),
    XLMUSD("XLM-USD"),
    ETCUSD("ETC-USD");

    public final String productName;

    private static final HashMap<String, CBProduct> productMap = new HashMap<>();

    static {
        for (CBProduct p : values()) {
            productMap.put(p.productName, p);
        }
    }

    CBProduct(String productName) {
        this.productName = productName;
    }

    public static String[] getProductNames() {
        String[] productNames = new String[values().length];
        for (int i = 0; i < values().length; i++) {
            productNames[i] = values()[i].productName;
        }
        return productNames;
    }

    public static CBProduct from(String productNqme) {
        return productMap.get(productNqme);
    }

}
