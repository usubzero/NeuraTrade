package io.ethanfine.neuratrade.coinbase;

public enum CBTimeGranularity {

    MINUTE(60),
    MINUTE_FIVE(300),
    MINUTE_FIFTEEN(900),
    HOUR(3600),
    HOUR_SIX(21600),
    DAY(86400);

    public final int seconds;

    private CBTimeGranularity(int seconds) {
        this.seconds = seconds;
    }

}
