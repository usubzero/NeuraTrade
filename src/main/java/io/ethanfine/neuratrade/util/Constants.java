package io.ethanfine.neuratrade.util;

import io.ethanfine.neuratrade.coinbase.models.CBProduct;
import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;

public class Constants {

    public static String CB_API_URL = "https://api.pro.coinbase.com/";
    public static String CB_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0";

    public static String CB_API_ENDPOINT_TIME = "time";

    public static String CB_API_ENDPOINT_TICKER(CBProduct product) {
        return "products/" + product.productName + "/ticker";
    }
    public static String CB_API_ENDPOINT_HISTORIC_RATES(CBProduct product, long start, long end, CBTimeGranularity timeGranularity) {
        String isoStart = Util.convertToIsoFromEpoch(start);
        String isoEnd = Util.convertToIsoFromEpoch(end);
        return "products/" + product.productName + "/candles?start=" + isoStart  + "&end=" + isoEnd + "&granularity=" + timeGranularity.seconds;
    }

}
