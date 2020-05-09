package io.ethanfine.neuratrade.coinbase.models;

import io.ethanfine.neuratrade.Config;

public enum CBTimeGranularity {

    MINUTE(60),
    MINUTE_FIVE(300),
    MINUTE_FIFTEEN(900),
    HOUR(3600),
    HOUR_SIX(21600),
    DAY(86400);

    public final int seconds;

    CBTimeGranularity(int seconds) {
        this.seconds = seconds;
    }

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

}
