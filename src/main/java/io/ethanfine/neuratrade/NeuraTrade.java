package io.ethanfine.neuratrade;

import io.ethanfine.neuratrade.coinbase.CBPublicData;
import io.ethanfine.neuratrade.data.models.BarDataPoint;
import io.ethanfine.neuratrade.data.models.BarDataSeries;
import io.ethanfine.neuratrade.external_data.FNGPublicData;
import io.ethanfine.neuratrade.ui.UIMain;
import org.ta4j.core.BarSeries;

import java.util.Date;
import java.util.Map;

public class NeuraTrade {

    public static void main(String args[]) {
        BarSeries barSeries = CBPublicData.getRecentBarSeries(Config.shared.product, Config.shared.chartBarCount.value, Config.shared.timeGranularity);
        BarDataSeries barDataSeries = new BarDataSeries(Config.shared.product, barSeries, Config.shared.timeGranularity);
        for (int i = 0; i < barSeries.getBarCount(); i++) {
            BarDataPoint barDataPoint = barDataSeries.getBarDataPoint(i);
            System.out.println("Bar " + i + ": (Open Time: " + barDataPoint.bar.getBeginTime() + "Open: " + barDataPoint.bar.getOpenPrice() + ", Close: " + barDataPoint.bar.getClosePrice() + ", High: " + barDataPoint.bar.getHighPrice() + ", Low: " + barDataPoint.bar.getLowPrice() + ", Volume: " + barDataPoint.bar.getVolume() + ", RSI: " + barDataPoint.rsi + ", MACD: " + barDataPoint.macd + ", FNG I: " + barDataPoint.fngIndex + ")");
        }
        if (barSeries.isEmpty()) {
            System.out.println("Failed to retrieve BTC recent bar series.");
        }

        Map<Long, Integer> fngDataPoints = FNGPublicData.getFNGIndexDataPoints(830);
        for (Map.Entry<Long, Integer> fngDataPoint : fngDataPoints.entrySet()) {
            System.out.println("EPOCH: " + new Date(fngDataPoint.getKey() * 1000) + ", FNG Index: " + fngDataPoint.getValue());
        }
        new UIMain();
    }

}
