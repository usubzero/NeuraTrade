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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

public class UIMain implements ActionListener {

    JFrame frame;
    ChartPanel chartPanel;
    JLabel priceLabel;
    JLabel rsiLabel;
    JPanel parametersPanel;
    JComboBox productSelector;
    JComboBox granularitySelector;
    JComboBox barCountSelector;

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
        BarDataSeries recentBarDataSeries = new BarDataSeries(Config.shared.product, recentBarSeries);
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
        granularitySelector = loadParametersPanelSelector(CBTimeGranularity.values(), Config.shared.timeGranularity);
        parametersPanel.add(granularitySelector, BorderLayout.CENTER);
        barCountSelector = loadParametersPanelSelector(ChartBarCount.values(), Config.shared.chartBarCount);
        parametersPanel.add(barCountSelector, BorderLayout.EAST);
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
        XYDataset dataset = createTrainingChartDataset(barDataSeries);
        JFreeChart chart = createChart(barDataSeries, dataset);

        chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(Color.WHITE);
        frame.getContentPane().add(chartPanel, BorderLayout.CENTER);
    }

    private XYDataset createTrainingChartDataset(BarDataSeries barDataSeries) {
        XYSeries priceSeries = new XYSeries(barDataSeries.product.productName + " Price");
        XYSeries buySeries = new XYSeries(barDataSeries.product.productName + " Buys");
        XYSeries sellSeries = new XYSeries(barDataSeries.product.productName + " Sells");
        ArrayList<BarDataPoint> buyBars = barDataSeries.getDataPointsForBarAction(BarAction.BUY);
        ArrayList<BarDataPoint> sellBars = barDataSeries.getDataPointsForBarAction(BarAction.SELL);
        for (int i = 0; i < barDataSeries.getBarCount(); i++) {
            BarDataPoint barDPI = barDataSeries.getBarDataPoint(i);
            if (barDPI.bar.isBullish()) {
                double lowPrice = barDPI.bar.getLowPrice().doubleValue();
                priceSeries.add(i, lowPrice);
                if (buyBars.contains(barDPI)) {
                    buySeries.add(i, lowPrice);
                }
            } else {
                double highPrice = barDPI.bar.getHighPrice().doubleValue();
                priceSeries.add(i, highPrice);
                if (sellBars.contains(barDPI)) {
                    sellSeries.add(i, highPrice);
                }
            }
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(priceSeries);
        dataset.addSeries(buySeries);
        dataset.addSeries(sellSeries);
        return dataset;
    }

    private JFreeChart createChart(BarDataSeries barDataSeries, XYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYLineChart(
                barDataSeries.product.productName + " Training Data",
                "Date",
                "Price",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        NumberAxis priceRange = (NumberAxis) plot.getRangeAxis();
        double low = barDataSeries.getBarDataPointWithLowestLow().bar.getLowPrice().doubleValue();
        double high = barDataSeries.getBarDataPointWithHighestHigh().bar.getHighPrice().doubleValue();
        double volatility = (high - low) / low;
        double rangeLow = low - (low * volatility / 10); //TODO: find better solution to maximize use of screen real estate
        double rangeHigh = high + (high * volatility / 10);
        priceRange.setRange(rangeLow, rangeHigh);
        priceRange.setTickUnit(new NumberTickUnit((rangeHigh - rangeLow) / 5));
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        renderer.setSeriesStroke(0, new BasicStroke(1.0f));
        renderer.setSeriesPaint(1, Color.GREEN);
        renderer.setSeriesLinesVisible(1, false);
        renderer.setSeriesPaint(2, Color.RED);
        renderer.setSeriesLinesVisible(2, false);
        renderer.setSeriesShape(0, new Rectangle(2, 2));
        Rectangle buySellShape = new Rectangle(4, 4);
        renderer.setSeriesShape(1, buySellShape);
        renderer.setSeriesShape(2, buySellShape);
        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);

        chart.getLegend().setFrame(BlockBorder.NONE);
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
        BarDataSeries recentBarDataSeries = new BarDataSeries(Config.shared.product, recentBarSeries);
        initiateChart(recentBarDataSeries);
    }

    private void beginRefreshCycle() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000 ); /* TODO: find suitable time; must be > 1000ms if retrieving bar series
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
