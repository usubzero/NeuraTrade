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
import org.jfree.chart.plot.CombinedDomainXYPlot;
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

public class UIMain implements ActionListener {

    JFrame frame;
    ChartPanel chartPanel;
    JLabel priceLabel;
    JLabel rsiLabel;
    JPanel parametersPanel;
    JComboBox productSelector;
    JComboBox barCountSelector;
    JComboBox granularitySelector;

    /**
     * Instantiates a JFrame for the app, loads the frames content, and makes this frame visible.
     */
    public UIMain() {
        frame = new JFrame("NeuraTrade");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 300);
        frame.setPreferredSize(new Dimension(1000, 500));
        loadFrameContent();
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Loads the ticker price and RSI labels along with the parameters panel. Then initializes a chart based off the
     * most recent bar series and begins the UI refresh cycle.
     */
    private void loadFrameContent() {
        loadTickerPriceLabel();
        loadRSILabel();
        loadParametersPanel();

        BarSeries recentBarSeries = CBPublicData.getRecentBarSeries(
                Config.shared.product,
                Config.shared.chartBarCount.value,
                Config.shared.timeGranularity
        );
        BarDataSeries recentBarDataSeries = new BarDataSeries(
                Config.shared.product,
                recentBarSeries,
                Config.shared.timeGranularity
        );
        initiateChart(recentBarDataSeries);

        beginRefreshCycle();
    }

    /**
     * Retrieves the ticker price for the product selected in Config, creates a JLabel with the price as
     * such label's title, and adds the label to the frame.
     */
    private void loadTickerPriceLabel() {
        Double tickerPrice = CBPublicData.getTickerPrice(Config.shared.product);

        String labelTitle = tickerPrice == null ?
                "PRICE RETRIEVAL ERROR" :
                "$" + Util.formatDouble(tickerPrice, 2);

        priceLabel = new JLabel(labelTitle, JLabel.CENTER);
        priceLabel.setVerticalTextPosition(JLabel.BOTTOM);
        priceLabel.setHorizontalTextPosition(JLabel.CENTER);

        frame.add(priceLabel, BorderLayout.NORTH);
    }

    /**
     * Retrieves the most recent bar series for the product selected in Config, creates a JLabel with the RSI value
     * calculated from such bar series as such label's title, and adds the label to the frame.
     */
    private void loadRSILabel() {
        BarSeries recentBarSeries = CBPublicData.getRecentBarSeries(
                Config.shared.product,
                Config.shared.chartBarCount.value,
                Config.shared.timeGranularity
        );

        String labelTitle = "RSI RETRIEVAL ERROR";
        if (recentBarSeries != null && !recentBarSeries.isEmpty()) {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(recentBarSeries);
            RSIIndicator rsiIndicator = new RSIIndicator(closePrice, Config.shared.rsiCalculationTickCount);
            double rsi = rsiIndicator.getValue(Config.shared.chartBarCount.value - 1).doubleValue();
            labelTitle = "RSI: " + Util.formatDouble(rsi, 2);
        }

        rsiLabel = new JLabel(labelTitle, JLabel.CENTER);
        rsiLabel.setVerticalTextPosition(JLabel.BOTTOM);
        rsiLabel.setHorizontalTextPosition(JLabel.CENTER);

        frame.add(rsiLabel, BorderLayout.EAST);
    }

    /**
     * Instantiates a JPanel that allows for interactions with the Config parameters of product, bar count, and
     * time granularity. Adds such panel to the frame and all parameter JComboBox selectors to the panel.
     */
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

    /**
     * Instantiates a JComboBox with values as the options available and selectedVal as the default selected option.
     * Precondition: selectedVal is in values
     * @param values the options available on the JComboBox
     * @param selectedVal the default option selected on the JComboBox
     */
    private JComboBox loadParametersPanelSelector(Object[] values, Object selectedVal) {
        JComboBox selector = new JComboBox(values);
        selector.setSelectedIndex(Arrays.asList(values).indexOf(selectedVal));
        selector.addActionListener(this);
        return selector;
    }

    // TODO: move several methods below into model class
    /**
     * Initiate a ChartPanel based off a chart created that contains a price data set, a training data labels data set,
     * and potentially a fear and greed data set. This ChartPanel is configured and the chart is added to the frame.
     * @param barDataSeries The BarDataSeries to base the data off to create the chart.
     */
    private void initiateChart(BarDataSeries barDataSeries) {
        AbstractXYDataset priceDataset = createPriceDataSet(barDataSeries);
        XYDataset labelsDataset = createTrainingChartDataset(barDataSeries);
        XYDataset fngDataset = createFNGDataset(barDataSeries);
        JFreeChart chart = createChart(barDataSeries, priceDataset, labelsDataset,  fngDataset, Config.shared.timeGranularity);

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

    /**
     * Create an XY-Dataset for price data. This dataset contains time on the X axis and
     * (open, high, low, close, and volume) on the Y axis for every bar in barDataSeries.
     * @param barDataSeries the BarDataSeries to base the dataset on.
     * @return The XY-Dataset as an AbstractXYDataset.
     */
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

    /**
     * Create an XY-Dataset with a buy series and a sell series. The buy series is based on the BarDataPoints where
     * barDataSeries would buy the product corresponding to barDataSeries. The sell series is the same as the buy series
     * but for the sell BarAction.  The points in the buy and sell series contain time on the X axis and a price to
     * buy or sell at on the Y axis.
     * @param barDataSeries the BarDataSeries to base the dataset on.
     * @return The XY-Dataset containing a buy series and a sell series as an XYDataSet.
     */
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

    /**
     * Create an XY-Dataset with a fear and greed series. The fear and greed series is composed of hte fear and greed
     * index value for every bar in barDataSeries. The points in the fear and greed series contain time on the X axis
     * and the fear and greed index values on the Y axis.
     * @param barDataSeries the BarDataSeries to base the dataset on.
     * @return The XY-Dataset containing a fear and greed index series as an XYDataSet.
     */
    private XYDataset createFNGDataset(BarDataSeries barDataSeries) {
        XYSeries fngPoints = new XYSeries(barDataSeries.product.productName + " FNG Index");
        for (int i = 0; i < barDataSeries.getBarCount(); i++) {
            BarDataPoint bdpI = barDataSeries.getBarDataPoint(i);
            fngPoints.add(bdpI.bar.getBeginTime().toEpochSecond() * 1000, bdpI.fngIndex);
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(fngPoints);
        return dataset;
    }

    /**
     * Create a JFreeChart that displays priceDataSet, labelsDataSet, and potentially fngDataset. priceDataSet is
     * displayed as candlestick bars, labelsSet is displayed with blue circles on the buy prices and pink circles on the
     * sell prices, and fngDataset is displayed only if the timeGranularity is the DAY CBTimeGranularity and is
     * displayed as a subplot plotting time on the X axis vs fear and greed index values on the Y axis. The main plot
     * displays time on the X axis and the range of prices in barDataSeries on the Y axis.
     * @param barDataSeries the BarDataSeries to construct the chart based on.
     * @param priceDataset the priceDataset to construct the chart based on and to include in the main plot.
     * @param labelsDataset the labelsDataset to include in the main plot.
     * @param fngDataset the fngDataset to potentially construct a subplot to be included in the chart.
     * @param timeGranularity the timeGranularity of each bar.
     * @return the JFreeChart created with the main plot and potentially a subplot.
     */
    private JFreeChart createChart(BarDataSeries barDataSeries,
                                   AbstractXYDataset priceDataset,
                                   XYDataset labelsDataset,
                                   XYDataset fngDataset,
                                   CBTimeGranularity timeGranularity) {
        DateAxis domainAxis = new DateAxis("Date");

        NumberAxis priceRangeAxis = new NumberAxis("Price");
        CandlestickRenderer priceRenderer = new CandlestickRenderer();

        final XYPlot mainPlot = new XYPlot(priceDataset, domainAxis, priceRangeAxis, priceRenderer);
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

        priceRenderer.setSeriesPaint(0, Color.BLACK);
        priceRenderer.setDrawVolume(true);
        priceRangeAxis.setAutoRangeIncludesZero(false);

        XYPlot fngPlot = null;
        if (timeGranularity == CBTimeGranularity.DAY) {
            XYLineAndShapeRenderer fngRenderer = new XYLineAndShapeRenderer();
            NumberAxis fngRangeAxis = new NumberAxis("FNG Index");
            fngPlot = new XYPlot(fngDataset, domainAxis, fngRangeAxis, fngRenderer);
            fngRangeAxis.setAutoRangeIncludesZero(false);
            fngRenderer.setSeriesShape(0, new Rectangle(1, 1));
        }

        final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(domainAxis);
        plot.setGap(10);
        plot.add(mainPlot, 5);
        if (timeGranularity == CBTimeGranularity.DAY) {
            plot.add(fngPlot, 1);
        }

        final JFreeChart chart = new JFreeChart(
                Config.shared.product.productName,
                null, plot,
                false
        );
        chart.setTitle(barDataSeries.product.productName + " Training Data");
        return chart;
    }

    /**
     * Update the data displayed on all UI elements. Retrieve new data and display relevant data on the ticker price
     * label, on the RSI label, and refresh the chart panel by removing it and generating a new one.
     */
    private void refreshUIDynamicElements() {
        Double tickerPrice = CBPublicData.getTickerPrice(Config.shared.product);
        String newLabelTitle = (tickerPrice == null) ?
                "PRICE RETRIEVAL ERROR" :
                "$" + Util.formatDouble(tickerPrice, 2);
        priceLabel.setText(newLabelTitle);

        BarSeries recentBarSeries = CBPublicData.getRecentBarSeries(
                Config.shared.product,
                Config.shared.chartBarCount.value,
                Config.shared.timeGranularity
        );

        String rsiLabelTitle = "RSI RETRIEVAL ERROR";
        if (recentBarSeries != null && !recentBarSeries.isEmpty()) {
            ClosePriceIndicator closePrice = new ClosePriceIndicator(recentBarSeries);
            RSIIndicator rsiIndicator = new RSIIndicator(closePrice, Config.shared.rsiCalculationTickCount);
            double rsi = rsiIndicator.getValue(Config.shared.chartBarCount.value - 1).doubleValue();
            rsiLabelTitle = "RSI: " + Util.formatDouble(rsi, 2);
        }
        rsiLabel.setText(rsiLabelTitle);

        if (chartPanel != null) {
            frame.remove(chartPanel);
        }
        BarDataSeries recentBarDataSeries = new BarDataSeries(
                Config.shared.product,
                recentBarSeries,
                Config.shared.timeGranularity
        );
        initiateChart(recentBarDataSeries);
    }

    /**
     * Create a thread which sleeps other than every 20 seconds, when it is awake simply to refresh the dynamic elements
     * of the UI.
     */
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

    /**
     * Detect actions on the chart panel.
     * @param e the action that was performed onn the panel.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == productSelector) {
            Config.shared.product = (CBProduct) productSelector.getSelectedItem();
        } else if (e.getSource() == granularitySelector) {
            Config.shared.timeGranularity = (CBTimeGranularity) granularitySelector.getSelectedItem();
        } else if (e.getSource() == barCountSelector) {
            Config.shared.chartBarCount = (ChartBarCount) barCountSelector.getSelectedItem();
        }

        refreshUIDynamicElements();
    }

}
