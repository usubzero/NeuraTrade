package io.ethanfine.neuratrade.ui.models;

public enum ChartBarCount {

    /**
     * The possible number of bars to be displayed on a chart enumerated along with their respective numerical values.
     */
    FIFTY(50),
    HUNDRED(100),
    HUNDRED_TWO(200),
    HUNDRED_THREE(300);

    public final int value;

    ChartBarCount(int value) {
        this.value = value;
    }

}
