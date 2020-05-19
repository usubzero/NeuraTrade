package io.ethanfine.neuratrade.ui;

import io.ethanfine.neuratrade.Config;
import io.ethanfine.neuratrade.coinbase.models.CBProduct;
import io.ethanfine.neuratrade.coinbase.CBPublicData;
import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;
import io.ethanfine.neuratrade.data.models.BarAction;
import io.ethanfine.neuratrade.data.models.BarDataPoint;
import io.ethanfine.neuratrade.data.models.BarDataSeries;
import io.ethanfine.neuratrade.ui.models.ChartBarCount;
import io.ethanfine.neuratrade.util.Util;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.labels.CrosshairLabelGenerator;
import org.jfree.chart.panel.CrosshairOverlay;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.*;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class UIMain implements ActionListener {

    JFrame frame;
    ChartPanel chartPanel;
    JLabel priceLabel;
    JLabel rsiLabel;
    JPanel parametersPanel;
    JComboBox productSelector;
    JComboBox barCountSelector;
    JComboBox granularitySelector;

    public UIMain() {
        frame = new JFrame("NeuraTrade");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 300);
        frame.setPreferredSize(new Dimension(1000, 500));
        loadFrameContent();
        frame.pack();
        frame.setVisible(true);
    }

    private void loadFrameContent() {
        loadTickerPriceLabel();
        loadRSILabel();
        loadParametersPanel();

        BarSeries recentBarSeries = CBPublicData.getRecentBarSeries(Config.shared.product, Config.shared.chartBarCount.value, Config.shared.timeGranularity);
        BarDataSeries recentBarDataSeries = new BarDataSeries(Config.shared.product, recentBarSeries, Config.shared.timeGranularity);
        initiateChart(recentBarDataSeries);

        beginRefreshCycle();
    }

    private void loadTickerPriceLabel() {
        Double tickerPrice = CBPublicData.getTickerPrice(Config.shared.product);
        String labelTitle = tickerPrice == null ? "PRICE RETRIEVAL ERROR" : "$" + Util.formatDouble(tickerPrice, 2);
        priceLabel = new JLabel(labelTitle, JLabel.CENTER);
        priceLabel.setVerticalTextPosition(JLabel.BOTTOM);
        priceLabel.setHorizontalTextPosition(JLabel.CENTER);
        frame.add(priceLabel, BorderLayout.NORTH);
    }

    private void loadRSILabel() {
        BarSeries recentBarSeries = CBPublicData.getRecentBarSeries(Config.shared.product, Config.shared.chartBarCount.value, Config.shared.timeGranularity);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(recentBarSeries);
        RSIIndicator rsiIndicator = new RSIIndicator(closePrice, Config.shared.rsiCalculationTickCount);
        double rsi = rsiIndicator.getValue(Config.shared.chartBarCount.value - 1).doubleValue();
        String labelTitle = recentBarSeries.isEmpty() ? "RSI RETRIEVAL ERROR" : "RSI:" + Util.formatDouble(rsi, 2);
        rsiLabel = new JLabel(labelTitle, JLabel.CENTER);
        rsiLabel.setVerticalTextPosition(JLabel.BOTTOM);
        rsiLabel.setHorizontalTextPosition(JLabel.CENTER);
        frame.add(rsiLabel, BorderLayout.EAST);
    }

    private void loadParametersPanel() {
        parametersPanel = new JPanel();
        parametersPanel.setPreferredSize(new Dimension(400, 40));
        frame.getContentPane().add(parametersPanel, BorderLayout.SOUTH);
        productSelector = loadParametersPanelSelector(CBProduct.values(), Config.shared.product);
        parametersPanel.add(productSelector, BorderLayout.WEST);
        barCountSelector = loadParametersPanelSelector(ChartBarCount.values(), Config.shared.chartBarCount);
        parametersPanel.add(barCountSelector, BorderLayout.CENTER);
        granularitySelector = loadParametersPanelSelector(CBTimeGranularity.values(), Config.shared.timeGranularity);
        parametersPanel.add(granularitySelector, BorderLayout.EAST);
    }

    /*
    Precondition: selectedVal is in values
     */
    private JComboBox loadParametersPanelSelector(Object[] values, Object selectedVal) {
        JComboBox selector = new JComboBox(values);
        selector.setSelectedIndex(Arrays.asList(values).indexOf(selectedVal));
        selector.addActionListener(this);
        return selector;
    }

    private void initiateChart(BarDataSeries barDataSeries) {

        AbstractXYDataset priceDataset = createPriceDataSet(barDataSeries);
        XYDataset labelsDataset = createTrainingChartDataset(barDataSeries);
        JFreeChart chart = createChart(barDataSeries, priceDataset, labelsDataset);

        chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setHorizontalAxisTrace(true);
        chartPanel.setVerticalAxisTrace(true);
        chartPanel.addChartMouseListener(new ChartMouseListener() {

            @Override
            public void chartMouseClicked(ChartMouseEvent e) {
                final ChartEntity entity = e.getEntity();
                System.out.println(entity + " " + entity.getArea());
            }

            @Override
            public void chartMouseMoved(ChartMouseEvent e) {
            }
        });

        frame.getContentPane().add(chartPanel, BorderLayout.CENTER);
    }

    private AbstractXYDataset createPriceDataSet(BarDataSeries barDataSeries) {
        OHLCDataItem[] data  = new OHLCDataItem[barDataSeries.getBarCount()];

        for (int i = 0; i < barDataSeries.getBarCount(); i++) {
            BarDataPoint bdpI = barDataSeries.getBarDataPoint(i);
            data[i] = new OHLCDataItem(Date.from(bdpI.bar.getBeginTime().toInstant()),
                    bdpI.bar.getOpenPrice().doubleValue(),
                    bdpI.bar.getHighPrice().doubleValue(),
                    bdpI.bar.getLowPrice().doubleValue(),
                    bdpI.bar.getClosePrice().doubleValue(),
                    bdpI.bar.getVolume().doubleValue()
            );
        }

        return new DefaultOHLCDataset(Config.shared.product + " Prices", data);
    }

    private XYDataset createTrainingChartDataset(BarDataSeries barDataSeries) {
        XYSeries buySeries = new XYSeries(barDataSeries.product.productName + " Buys");
        XYSeries sellSeries = new XYSeries(barDataSeries.product.productName + " Sells");
        ArrayList<BarDataPoint> buyBars = barDataSeries.getDataPointsForBarAction(BarAction.BUY);
        ArrayList<BarDataPoint> sellBars = barDataSeries.getDataPointsForBarAction(BarAction.SELL);
        for (int i = 0; i < barDataSeries.getBarCount(); i++) {
            BarDataPoint bdpI = barDataSeries.getBarDataPoint(i);
            if (buyBars.contains(bdpI)) {
                buySeries.add(bdpI.bar.getBeginTime().toEpochSecond() * 1000, bdpI.bar.getLowPrice().doubleValue());
            }
            if (sellBars.contains(bdpI)) {
                sellSeries.add(bdpI.bar.getBeginTime().toEpochSecond() * 1000, bdpI.bar.getHighPrice().doubleValue());
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(buySeries);
        dataset.addSeries(sellSeries);
        return dataset;
    }

    private JFreeChart createChart(BarDataSeries barDataSeries, AbstractXYDataset priceDataset, XYDataset labelsDataset) {
        DateAxis domainAxis = new DateAxis("Date");
        NumberAxis  rangeAxis = new NumberAxis("Price");
        CandlestickRenderer renderer = new CandlestickRenderer();

        XYPlot mainPlot = new XYPlot(priceDataset, domainAxis, rangeAxis, renderer);

        mainPlot.setDataset(1, labelsDataset);
        XYLineAndShapeRenderer labelsRenderer = new XYLineAndShapeRenderer(false, true);
        labelsRenderer.setSeriesPaint(0, Color.BLUE);
        labelsRenderer.setSeriesPaint(1, Color.MAGENTA);
        Ellipse2D ellipse = new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0);
        labelsRenderer.setSeriesShape(0, ellipse);
        labelsRenderer.setSeriesShape(1, ellipse);
        mainPlot.setRenderer(1, labelsRenderer);

//        final long ONE_DAY = 24 * 60 * 60 * 1000;
//        XYLineAndShapeRenderer maRenderer = new XYLineAndShapeRenderer(true, false);
//        XYDataset maDataset  = MovingAverage.createMovingAverage(priceDataset, "MA", 200 * ONE_DAY, 0);
//        mainPlot.setRenderer(2, maRenderer);
//        mainPlot.setDataset (2, maDataset);

        renderer.setSeriesPaint(0, Color.BLACK);
        renderer.setDrawVolume(true);
        rangeAxis.setAutoRangeIncludesZero(false);

        JFreeChart chart = new JFreeChart(Config.shared.product.productName, null, mainPlot, false);
        chart.setTitle(barDataSeries.product.productName + " Training Data");
        return chart;
    }

    private void refreshUIDynamicElements() {
        Double tickerPrice = CBPublicData.getTickerPrice(Config.shared.product);
        String newLabelTitle = (tickerPrice == null) ? "PRICE RETRIEVAL ERROR" : "$" + Util.formatDouble(tickerPrice, 2);
        priceLabel.setText(newLabelTitle);

        BarSeries recentBarSeries = CBPublicData.getRecentBarSeries(Config.shared.product, Config.shared.chartBarCount.value, Config.shared.timeGranularity);
        ClosePriceIndicator closePrice = new ClosePriceIndicator(recentBarSeries);
        RSIIndicator rsiIndicator = new RSIIndicator(closePrice, Config.shared.rsiCalculationTickCount);
        double rsi = (recentBarSeries.isEmpty()) ? 0 : rsiIndicator.getValue(Config.shared.chartBarCount.value - 1).doubleValue();
        String rsiLabelTitle = (recentBarSeries.isEmpty()) ? "RSI RETRIEVAL ERROR" : "RSI:" + Util.formatDouble(rsi, 2);
        rsiLabel.setText(rsiLabelTitle);

        if (chartPanel != null) {
            frame.remove(chartPanel);
        }
        BarDataSeries recentBarDataSeries = new BarDataSeries(Config.shared.product, recentBarSeries, Config.shared.timeGranularity);
        initiateChart(recentBarDataSeries);
    }

    private void beginRefreshCycle() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(20000 ); /* TODO: find suitable time; must be > 1000ms if retrieving bar series
                    due to API restrictions, but would want to change so that bar series isn't retrieved every time ticker is */
                    refreshUIDynamicElements();
                } catch (Exception e) {
                    priceLabel.setText("PRICE RETRIEVAL ERROR");
                    rsiLabel.setText("RSI RETRIEVAL ERROR");
                }
            }
        }).start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == productSelector) {
            CBProduct newProduct = (CBProduct) productSelector.getSelectedItem();
            Config.shared.product = newProduct;
        } else if (e.getSource() == granularitySelector) {
            CBTimeGranularity newGranularity = (CBTimeGranularity) granularitySelector.getSelectedItem();
            Config.shared.timeGranularity = newGranularity;
        } else if (e.getSource() == barCountSelector) {
            ChartBarCount newBarCount = (ChartBarCount) barCountSelector.getSelectedItem();
            Config.shared.chartBarCount = newBarCount;
        }

        refreshUIDynamicElements();
    }

}
