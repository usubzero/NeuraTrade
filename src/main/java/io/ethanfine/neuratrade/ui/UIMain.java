package io.ethanfine.neuratrade.ui;

import io.ethanfine.neuratrade.Config;
import io.ethanfine.neuratrade.coinbase.CBPublicData;
import io.ethanfine.neuratrade.data.models.BarDataSeries;
import io.ethanfine.neuratrade.ui.generators.InputsChartGenerator;
import io.ethanfine.neuratrade.ui.generators.MenuBarGenerator;
import io.ethanfine.neuratrade.ui.generators.ParametersPanelManager;
import io.ethanfine.neuratrade.util.Util;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.entity.ChartEntity;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import javax.swing.*;

import java.awt.*;

public class UIMain {

    public JFrame frame;
    ChartPanel chartPanel;
    JLabel priceLabel;
    JLabel rsiLabel;
    ParametersPanelManager parametersPanelManager;
    InputsChartGenerator inputsChartGenerator;

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
        frame.setJMenuBar(new MenuBarGenerator(this).generateMenuBar());
        loadTickerPriceLabel();
        loadRSILabel();
        parametersPanelManager = new ParametersPanelManager(this);

        BarSeries recentBarSeries = State.getRecentBarSeries();
        if (recentBarSeries != null && !recentBarSeries.isEmpty()) {
            BarDataSeries bds = new BarDataSeries(
                    Config.shared.product,
                    recentBarSeries,
                    Config.shared.timeGranularity
            );
            // TODO: abstract away BarDataSeries constructor with config values
            initiateInputChartsPanel(bds);
        }

        beginRefreshCycle();
    }

    /**
     * Retrieves the ticker price for the product selected in Config and creates a user-friendly String representation
     * of it.
     * @return User-friendly String representation of ticker price.
     */
    private String tickerPriceLabelTitle() {
        Double tickerPrice = CBPublicData.getTickerPrice(Config.shared.product);
        return tickerPrice == null ?
                "PRICE RETRIEVAL ERROR" :
                "Live Market Price: $" + Util.formatDouble(tickerPrice, 2);
    }

    /**
     * Creates a JLabel with a ticker price as such label's title, and adds the label to the frame.
     */
    private void loadTickerPriceLabel() {
        priceLabel = new JLabel(tickerPriceLabelTitle(), JLabel.CENTER);
        priceLabel.setVerticalTextPosition(JLabel.BOTTOM);
        priceLabel.setHorizontalTextPosition(JLabel.CENTER);

        frame.add(priceLabel, BorderLayout.NORTH);
    }

    /**
     * Retrieves the most recent bar series for the product selected in Config, creates a JLabel with the RSI value
     * calculated from such bar series as such label's title, and adds the label to the frame.
     */
    private void loadRSILabel() {
        BarSeries recentBarSeries = State.getRecentBarSeries();

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

    // TODO: move several methods below into model class
    /**
     * Initiate a ChartPanel based off a chart created that contains a price data set, a training data labels data set,
     * and potentially a fear and greed data set. This ChartPanel is configured and the chart is added to the frame.
     * TODO: update doc
     * @param barDataSeries The BarDataSeries to base the data off to create the chart.
     */
    private void initiateInputChartsPanel(BarDataSeries barDataSeries) {
//        AbstractXYDataset priceDataset = DataSetUtil.createPriceDataSet(barDataSeries);
//        XYDataset labelsDataset = DataSetUtil.createTrainingChartDataset(barDataSeries);
//        XYDataset fngDataset = DataSetUtil.createFNGDataset(barDataSeries);
//        JFreeChart chart = createChart(barDataSeries, priceDataset, labelsDataset,  fngDataset, Config.shared.timeGranularity);
        inputsChartGenerator = new InputsChartGenerator(barDataSeries);
        JFreeChart inputsChart = inputsChartGenerator.generateChart();
        chartPanel = new ChartPanel(inputsChart);
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
     * Update the data displayed on all UI elements. Retrieve new data and display relevant data on the ticker price
     * label, on the RSI label, and refresh the chart panel by removing it and generating a new one.
     */
    public void refreshUIDynamicElements() {
        if (State.displayBDSisImported()) {
            // TODO: change to display file name of file from which data was imported
            priceLabel.setVisible(false);
            rsiLabel.setVisible(false);
        } else {
            priceLabel.setText(tickerPriceLabelTitle());
            priceLabel.setVisible(true);
            rsiLabel.setVisible(true);
        }

//        Config.shared.timeGranularity = CBTimeGranularity.MINUTE;
//        BarSeries recentBarSeries = getRecentBarSeries();
        BarDataSeries bds = State.getDisplayBDS();
        String rsiLabelTitle = "RSI RETRIEVAL ERROR";
        if (bds != null) {
            double rsi = bds.getBarDataPoint(bds.getBarCount() - 1).rsi;
            rsiLabelTitle = "RSI: " + Util.formatDouble(rsi, 2);
            rsiLabel.setText(rsiLabelTitle);

            if (chartPanel != null) {
                frame.remove(chartPanel);
            }
            initiateInputChartsPanel(bds);
        }

        parametersPanelManager.refreshParametersPanel();
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

}
