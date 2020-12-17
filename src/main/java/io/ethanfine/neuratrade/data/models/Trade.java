package io.ethanfine.neuratrade.data.models;

public class Trade {

    public double epoch;
    public double price;
    public BarAction barAction;

    public Trade(double epoch, double price, BarAction barAction) {
        this.epoch = epoch;
        this.price = price;
        this.barAction = barAction;
    }

}