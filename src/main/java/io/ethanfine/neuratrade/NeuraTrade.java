package io.ethanfine.neuratrade;

import io.ethanfine.neuratrade.coinbase.CBProduct;
import io.ethanfine.neuratrade.coinbase.CBPublicData;
import io.ethanfine.neuratrade.coinbase.CBTimeGranularity;
import io.ethanfine.neuratrade.ui.UserInterfaceMain;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;

public class NeuraTrade {

    public static void main(String args[]) {
        try {
            BarSeries barSeries = CBPublicData.getRecentBarSeries(CBProduct.BTCUSD, 150, CBTimeGranularity.DAY);
            for (int i = 0; i < barSeries.getBarCount(); i++) {
                Bar bar = barSeries.getBar(i);
                System.out.println("Bar " + i + ": (Open: " + bar.getOpenPrice() + ", Close: " + bar.getClosePrice() + ", High: " + bar.getHighPrice() + ", Low: " + bar.getLowPrice() + ", Volume: " + bar.getVolume() + ")");
            }
        } catch (Exception exception) {
            System.out.println("Failed to retrieve BTC bar series.");
        }

        new UserInterfaceMain();
    }

}
