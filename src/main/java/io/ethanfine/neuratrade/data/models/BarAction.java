package io.ethanfine.neuratrade.data.models;

import java.util.HashMap;

public enum BarAction {

    /**
     * Possible actions to execute throughout the course of a bar's duration.
     */
    BUY("Buy"),
    HOLD("Hold"),
    SELL("Sell");

    public final String stringRep;

    private static final HashMap<String, BarAction> actionMap = new HashMap<>();

    /*
    Populates product map on static instance initialization
     */
    static {
        for (BarAction a : values()) {
            actionMap.put(a.stringRep, a);
        }
    }

    BarAction(String stringRep) {
        this.stringRep = stringRep;
    }

    public static BarAction from(String stringRep) {
        return actionMap.get(stringRep);
    }

}
