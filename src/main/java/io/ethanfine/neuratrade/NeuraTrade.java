package io.ethanfine.neuratrade;

import io.ethanfine.neuratrade.coinbase.CBPublicData;
import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;
import io.ethanfine.neuratrade.data.models.BarDataPoint;
import io.ethanfine.neuratrade.data.models.BarDataSeries;
import io.ethanfine.neuratrade.ui.UIMain;
import org.ta4j.core.BarSeries;

public class NeuraTrade {

    public static void main(String args[]) {
        BarSeries barSeries = CBPublicData.getRecentBarSeries(Config.shared.product, Config.shared.chartBarCount.value, CBTimeGranularity.DAY);
        BarDataSeries barDataSeries = new BarDataSeries(Config.shared.product, barSeries);
        for (int i = 0; i < barSeries.getBarCount(); i++) {
            BarDataPoint barDataPoint = barDataSeries.getBarDataPoint(i);
            System.out.println("Bar " + i + ": (Open Time: " + barDataPoint.bar.getBeginTime() + "Open: " + barDataPoint.bar.getOpenPrice() + ", Close: " + barDataPoint.bar.getClosePrice() + ", High: " + barDataPoint.bar.getHighPrice() + ", Low: " + barDataPoint.bar.getLowPrice() + ", Volume: " + barDataPoint.bar.getVolume() + ", RSI: " + barDataPoint.rsi + ", MACD: " + barDataPoint.macd + ")");
        }
        if (barSeries.isEmpty()) {
            System.out.println("Failed to retrieve BTC recent bar series.");
        }

        new UIMain();
    }

}
