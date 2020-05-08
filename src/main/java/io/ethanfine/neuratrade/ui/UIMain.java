package io.ethanfine.neuratrade.ui;

import io.ethanfine.neuratrade.Config;
import io.ethanfine.neuratrade.coinbase.CBProduct;
import io.ethanfine.neuratrade.coinbase.CBPublicData;
import io.ethanfine.neuratrade.coinbase.CBTimeGranularity;
import io.ethanfine.neuratrade.data.models.BarAction;
import io.ethanfine.neuratrade.data.models.BarDataPoint;
import io.ethanfine.neuratrade.data.models.BarDataSeries;
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

public class UIMain implements ActionListener {

    JFrame frame;
    ChartPanel chartPanel;
    JLabel priceLabel;
    JLabel rsiLabel;
    JComboBox productSelector;

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
        loadProductSelector();

        try {
            int barCount = 100;
            BarSeries recentBarSeries = CBPublicData.getRecentBarSeries(Config.shared.product, barCount, CBTimeGranularity.HOUR_SIX);
            BarDataSeries recentBarDataSeries = new BarDataSeries(Config.shared.product, recentBarSeries);
            recentBarDataSeries.labelBarActions(1, 0.3);
            initiateChart(recentBarDataSeries);
        } catch (Exception e) {
            // TODO
        }
        beginRefreshCycle();
    }

    private void loadTickerPriceLabel() {
        double tickerPrice = 0;
        boolean errorRetrievingTickerPrice = false;
        try {
            tickerPrice = CBPublicData.getTickerPrice(Config.shared.product);
        } catch (Exception e) {
            errorRetrievingTickerPrice = true;
        }
        String labelTitle = errorRetrievingTickerPrice ? "PRICE RETRIEVAL ERROR" : "$" + tickerPrice;
        priceLabel = new JLabel(labelTitle, JLabel.CENTER);
        priceLabel.setVerticalTextPosition(JLabel.BOTTOM);
        priceLabel.setHorizontalTextPosition(JLabel.CENTER);
        frame.add(priceLabel, BorderLayout.NORTH);
    }

    private void loadRSILabel() {
        double rsi = 0.0;
        boolean errorRetrievingRecentBars = false;
        try {
            int barCount = 300;
            BarSeries recentBarSeries = CBPublicData.getRecentBarSeries(Config.shared.product, barCount, CBTimeGranularity.HOUR_SIX);
            ClosePriceIndicator closePrice = new ClosePriceIndicator(recentBarSeries);
            RSIIndicator rsiIndicator = new RSIIndicator(closePrice, Config.shared.rsiCalculationTickCount);
            rsi = rsiIndicator.getValue(barCount - 1).doubleValue();
        } catch (Exception e) {
            errorRetrievingRecentBars = true;
        }
        String labelTitle = errorRetrievingRecentBars ? "RSI RETRIEVAL ERROR" : "RSI:" + rsi;
        rsiLabel = new JLabel(labelTitle, JLabel.CENTER);
        rsiLabel.setVerticalTextPosition(JLabel.BOTTOM);
        rsiLabel.setHorizontalTextPosition(JLabel.CENTER);
        frame.add(rsiLabel, BorderLayout.EAST);
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
        try {
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
        } catch (Exception e) {
            return null;
            // TODO
        }
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
        double rangeLow = low + (10 - low % 10);
        double rangeHigh = high + (10 - high % 10);
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

    private void beginRefreshCycle() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000 ); /* TODO: find suitable time; must be > 1000ms if retrieving bar series
                    due to API restrictions, but would want to change so that bar series isn't retrieved every time ticker is */
                    String newLabelTitle = "$" + CBPublicData.getTickerPrice(Config.shared.product);
                    priceLabel.setText(newLabelTitle);

                    BarSeries recentBarSeries = CBPublicData.getRecentBarSeries(Config.shared.product, 300, CBTimeGranularity.MINUTE);
                    ClosePriceIndicator closePrice = new ClosePriceIndicator(recentBarSeries);
                    RSIIndicator rsiIndicator = new RSIIndicator(closePrice, Config.shared.rsiCalculationTickCount);
                    double rsi = rsiIndicator.getValue(299).doubleValue();
                    String rsiLabelTitle = "RSI:" + rsi;
                    rsiLabel.setText(rsiLabelTitle);

                    BarDataSeries recentBarDataSeries = new BarDataSeries(Config.shared.product, recentBarSeries);
                    recentBarDataSeries.labelBarActions(0.3, 0.3);

                    if (chartPanel != null) {
                        frame.remove(chartPanel);
                    }
                    initiateChart(recentBarDataSeries); // TODO: update chart if parameters haven't changed rather than regenerating
                } catch (Exception e) {
                    priceLabel.setText("PRICE RETRIEVAL ERROR");
                    rsiLabel.setText("RSI RETRIEVAL ERROR");
                }
            }
        }).start();
    }

    private void loadProductSelector() {
        productSelector = new JComboBox(CBProduct.values());
        productSelector.setSelectedIndex(0);
        productSelector.addActionListener(this);
        frame.getContentPane().add(productSelector, BorderLayout.SOUTH);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JComboBox) {
            CBProduct newProduct = (CBProduct) productSelector.getSelectedItem();
            Config.shared.product = newProduct;
        }
    }
}
