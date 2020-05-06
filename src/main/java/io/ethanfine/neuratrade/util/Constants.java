package io.ethanfine.neuratrade.util;

public class Constants {

    public static String CB_API_URL = "https://api.pro.coinbase.com/";
    public static String CB_USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0";

    public static String CB_API_ENDPOINT_TICKER(String product) {
        return "products/" + product + "/ticker";
    }

}
