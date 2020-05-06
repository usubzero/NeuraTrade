package io.ethanfine.neuratrade;

import io.ethanfine.neuratrade.coinbase.CBPublicData;

public class NeuraTrade {

    public static void main(String args[]) {
        try {
            CBPublicData.getBTCTicker();
        } catch (Exception exception) {
            System.out.println("Failed to retrieve BTC ticker.");
        }
    }

}
