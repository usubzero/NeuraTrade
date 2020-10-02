package io.ethanfine.neuratrade.coinbase.models;

import java.util.HashMap;

/*
Coinbase products enumerated along with their Coinbase recognizable product name.
A "Coinbase recognizable product name" is one that can be included in Coinbase API calls without an error being returned
 */
public enum CBProduct {

    BTCUSD("BTC-USD"),
    ETHUSD("ETH-USD"),
    BCHUSD("BCH-USD"),
    LTCUSD("LTC-USD"),
    LINKUSD("LINK-USD"),
    XLMUSD("XLM-USD"),
    ALGOUSD("ALGO-USD"),
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
        return (String[]) productMap.keySet().toArray();
    }

    public static CBProduct from(String productName) {
        return productMap.get(productName);
    }

}
