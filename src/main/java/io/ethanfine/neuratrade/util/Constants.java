package io.ethanfine.neuratrade.util;

import io.ethanfine.neuratrade.coinbase.models.CBProduct;
import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;

public class Constants {

    /**
     * The Coinbase Pro API URL.
     */
    public static String CB_API_URL = "https://api.pro.coinbase.com/";
    /**
     * The User-Agent to use in HTTP requests with Coinbase Pro.
     */
    public static String CB_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0";

    /**
     * The Coinbase Pro API endpoint that provides the current epoch time value.
     */
    public static String CB_API_ENDPOINT_TIME = "time";

    /**
     * The Coinbase Pro API endpoint that gets the current ticker price of product.
     * @param product the product to form the endpoint to get the ticker price of.
     * @return the CB Pro API endpoint to get product's ticker price.
     */
    public static String CB_API_ENDPOINT_TICKER(CBProduct product) {
        return "products/" + product.productName + "/ticker";
    }

    /**
     * The Coinbase Pro API endpoint that gets candle data between a start and end time, associated with a product and
     * a time granularity.
     * @param product the product to form the endpoint to get the candle data of.
     * @param start the start time (epoch seconds) to append to the endpoint.
     * @param end the end time (epoch seconds) to append to the endpoint.
     * @param timeGranularity the time granularity to append to the endpoint.
     * @return the CB Pro API endpoint to get the candle data for the particular arguments.
     */
    public static String CB_API_ENDPOINT_HISTORIC_RATES(CBProduct product,
                                                        long start,
                                                        long end,
                                                        CBTimeGranularity timeGranularity) {
        String isoStart = Util.convertToIsoFromEpoch(start);
        String isoEnd = Util.convertToIsoFromEpoch(end);
        return "products/" + product.productName +
                "/candles?start=" + isoStart  +
                "&end=" + isoEnd +
                "&granularity=" + timeGranularity.seconds;
    }

    /**
     * The Alternative.me's Fear and Greed Index API endpoint for a number of index values determined by valueCount.
     * @param valueCount the number of Fear and Greed index values to append to the endpoint.
     * @return the Alternative.Me's F&G API endpoint to get the fear and greed index for a particular valueCount.
     */
    public static String FNG_API_DATA(int valueCount) {
        return "https://api.alternative.me/fng/?limit=" + valueCount;
    }

}
