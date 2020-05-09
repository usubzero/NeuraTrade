package io.ethanfine.neuratrade.ui.models;

public enum ChartBarCount {

    FIFTY(50),
    HUNDRED(100),
    HUNDRED_TWO(200),
    HUNDRED_THREE(300);

    public final int value;

    ChartBarCount(int value) {
        this.value = value;
    }

}
