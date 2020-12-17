package io.ethanfine.neuratrade.coinbase.models;

import io.ethanfine.neuratrade.Config;

import java.util.HashMap;

public enum CBTimeGranularity {

    /**
     * Possible time granularities enumerated along with their respective representation in seconds.
     */
    MINUTE(60),
    MINUTE_FIVE(300),
    MINUTE_FIFTEEN(900),
    HOUR(3600),
    HOUR_SIX(21600),
    DAY(86400);

    public final int seconds;

    private static final HashMap<Integer, CBTimeGranularity> timeGranularityMap = new HashMap<>();

    /*
    Populates time granularity map on static instance initialization
     */
    static {
        for (CBTimeGranularity tg : values()) {
            timeGranularityMap.put(tg.seconds, tg);
        }
    }

    CBTimeGranularity(int seconds) {
        this.seconds = seconds;
    }

    /**
     * This method gives the minimum percent difference between any successive pair of buy and sell orders in
     * generating training data. This will be defined as the buySellMinVolatility.
     * @return buySellMinVolatility.
     */
    public double buySellMinVolatility() {
        switch (this) {
            case MINUTE_FIVE:
                return Config.shared.minsFiveTimeGranularityBuySellMinVolatility;
            case MINUTE_FIFTEEN:
                return Config.shared.minsFifteenTimeGranularityBuySellMinVolatility;
            case HOUR:
                return Config.shared.hourTimeGranularityBuySellMinVolatility;
            case HOUR_SIX:
                return Config.shared.hourSixTimeGranularityBuySellMinVolatility;
            case DAY:
                return Config.shared.dayTimeGranularityBuySellMinVolatility;
            default:
                return Config.shared.minsTimeGranularityBuySellMinVolatility;
        }
    }

    /**
     * TODO: doc
     * @param timeGranularitySeconds
     * @return
     */
    public static CBTimeGranularity from(int timeGranularitySeconds) {
        return timeGranularityMap.get(timeGranularitySeconds);
    }

}
