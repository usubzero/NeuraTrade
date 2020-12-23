package io.ethanfine.neuratrade.ui;

import io.ethanfine.neuratrade.Config;
import io.ethanfine.neuratrade.coinbase.CBPublicData;
import io.ethanfine.neuratrade.data.models.BarAction;
import io.ethanfine.neuratrade.data.models.BarDataSeries;
import io.ethanfine.neuratrade.ui.generators.InputsChartGenerator;
import io.ethanfine.neuratrade.ui.generators.MenuBarGenerator;
import io.ethanfine.neuratrade.ui.generators.ParametersPanelManager;
import io.ethanfine.neuratrade.util.CSVIO;
import io.ethanfine.neuratrade.util.Util;
import org.jdesktop.swingx.JXDatePicker;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.ta4j.core.BarSeries;

import javax.swing.*;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class UIMain {

    public JFrame frame;
    ChartPanel chartPanel;
    JPanel sideLabelsPanel;
    JLabel priceLabel;
    JLabel rsiLabel;
    JLabel basisReturnLabel;
    JLabel predictionsReturnLabel;
    JLabel predictionLabel;
//    JXDatePicker startDatePicker;
//    JXDatePicker endDatePicker;
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

        BarSeries recentBarSeries = State.getRecentBarSeries();
        BarDataSeries bds = null;
        if (recentBarSeries != null && !recentBarSeries.isEmpty()) {
            bds = new BarDataSeries(
                    Config.shared.product,
                    recentBarSeries,
                    Config.shared.timeGranularity,
                    false
            );
        }

        loadLabels(bds);
        parametersPanelManager = new ParametersPanelManager(this);
        parametersPanelManager.predictionsToggler.setEnabled(State.canDisplayPredictions(bds));
        parametersPanelManager.predictionsToggler.setSelected(State.shouldDisplayPredictions(bds));
        initiateInputChartsPanel(bds);

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
     * Creates a JLabel with the most recent RSI value from bds as such label's title, and adds the label to the frame.
     * @param bds The BarDataSeries to base the RSI value off of.
     */
    private void loadRSILabel(BarDataSeries bds) {
        String labelTitle = "RSI RETRIEVAL ERROR";
        if (bds != null && bds.getBarCount() != 0) {
            double rsi = bds.getBarDataPoint(bds.getBarCount() - 1).rsi;
            labelTitle = "RSI: " + Util.formatDouble(rsi, 2);
        }

        rsiLabel = new JLabel(labelTitle, JLabel.CENTER);
        rsiLabel.setVerticalTextPosition(JLabel.BOTTOM);
        rsiLabel.setHorizontalTextPosition(JLabel.CENTER);

        sideLabelsPanel.add(rsiLabel);
    }

    // TODO: doc
    private void loadLabels(BarDataSeries bds) {
        sideLabelsPanel = new JPanel();
        sideLabelsPanel.setLayout(new BoxLayout(sideLabelsPanel, BoxLayout.Y_AXIS));
        frame.add(sideLabelsPanel, BorderLayout.EAST);

        predictionLabel = new JLabel(predictionLabelTitle(bds));
        predictionLabel.setVisible(State.shouldDisplayPredictions(bds));
        predictionLabel.setVerticalTextPosition(JLabel.BOTTOM);
        predictionLabel.setHorizontalTextPosition(JLabel.CENTER);
        sideLabelsPanel.add(predictionLabel);

        basisReturnLabel = new JLabel(returnLabelTitle("Basis", bds.basisReturn()));
        basisReturnLabel.setVerticalTextPosition(JLabel.BOTTOM);
        basisReturnLabel.setHorizontalTextPosition(JLabel.CENTER);
        sideLabelsPanel.add(basisReturnLabel);

        predictionsReturnLabel = new JLabel(returnLabelTitle("Predictions", bds.predictionsReturn()));
        predictionsReturnLabel.setVisible(State.shouldDisplayPredictions(bds));
        predictionsReturnLabel.setVerticalTextPosition(JLabel.BOTTOM);
        predictionsReturnLabel.setHorizontalTextPosition(JLabel.CENTER);
        sideLabelsPanel.add(predictionsReturnLabel);

//        startDatePicker = new JXDatePicker();
//        startDatePicker.setDate(Calendar.getInstance().getTime());
//        startDatePicker.setFormats(new SimpleDateFormat("MM/dd/yyyy"));
//        sideLabelsPanel.add(startDatePicker);
//        endDatePicker = new JXDatePicker();
//        endDatePicker.setDate(Calendar.getInstance().getTime());
//        endDatePicker.setFormats(new SimpleDateFormat("MM/dd/yyyy"));
//        sideLabelsPanel.add(endDatePicker);

        loadTickerPriceLabel();
        loadRSILabel(bds);
    }

    // TODO: move several methods below into model class
    /**
     * Initiate a ChartPanel based off a chart created that contains a price data set, a training data labels data set,
     * and potentially a fear and greed data set. This ChartPanel is configured and the chart is added to the frame.
     * TODO: update doc
     * @param barDataSeries The BarDataSeries to base the data off to create the chart.
     */
    private void initiateInputChartsPanel(BarDataSeries barDataSeries) {
        if (barDataSeries == null)
            return;

        ChartPanel oldPanel = chartPanel;
        inputsChartGenerator = new InputsChartGenerator(barDataSeries);
        JFreeChart inputsChart = inputsChartGenerator.generateChart();
        chartPanel = new ChartPanel(inputsChart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        chartPanel.setBackground(Color.WHITE);

        frame.getContentPane().add(chartPanel, BorderLayout.CENTER);
        if (oldPanel != null) {
            frame.getContentPane().remove(oldPanel);
        }
    }

    /**
     * Update the data displayed on all UI elements to reflect the data in bds. Update the ticker price
     * label, on the RSI label, and refresh the chart panel.
     * @param bds The BarDataSeries whose information should be displayed on the UI elements.
     */
    private void refreshUIDynamicElements(BarDataSeries bds) {
        parametersPanelManager.predictionsToggler.setEnabled(State.canDisplayPredictions(bds));
        parametersPanelManager.predictionsToggler.setSelected(State.shouldDisplayPredictions(bds));

//        State.DataType currentDataType = State.getCurrentDataType();
//        if (currentDataType == State.DataType.LIVE) {
//            priceLabel.setText(tickerPriceLabelTitle());
//            priceLabel.setVisible(true);
//            rsiLabel.setVisible(true);
//        }
        if (State.getCurrentDataType() != State.DataType.LIVE) {
            priceLabel.setVisible(false);
            rsiLabel.setVisible(false);
        } else {
            priceLabel.setText(tickerPriceLabelTitle());
            priceLabel.setVisible(true);
            rsiLabel.setVisible(true);
        }

        String rsiLabelTitle = "RSI RETRIEVAL ERROR";
        if (bds != null && bds.getBarCount() != 0) {
            basisReturnLabel.setText(returnLabelTitle("Basis", bds.basisReturn()));
            predictionsReturnLabel.setText(returnLabelTitle("Predictions", bds.predictionsReturn()));
            predictionsReturnLabel.setVisible(State.shouldDisplayPredictions(bds));

            predictionLabel.setText(predictionLabelTitle(bds));
            predictionLabel.setVisible(State.shouldDisplayPredictions(bds));

            double rsi = bds.getBarDataPoint(bds.getBarCount() - 1).rsi;
            rsiLabelTitle = "RSI: " + Util.formatDouble(rsi, 2);
            rsiLabel.setText(rsiLabelTitle);

            initiateInputChartsPanel(bds);
        }

        parametersPanelManager.refreshParametersPanel();
    }

    /**
     * Update the data to be displayed and refresh UI dynamic elements that display such data.
     */
    public void refresh() {
        BarDataSeries bds = State.getDisplayBDS();
        bds.labelTradePredictions(bds.timeGranularity.nnModel());
//        Date startPickerDate = (Date) startDatePicker.getEditor().getValue();
//        long startDate = startPickerDate.toInstant().getEpochSecond();
//        Date endPickerDate = (Date) endDatePicker.getEditor().getValue();
//        long endDate = endPickerDate.toInstant().getEpochSecond();
//        bds = CSVIO.readFile("historic_data/BTC-USD,21600.csv", startDate, endDate);
//        bds.labelTradePredictions(bds.timeGranularity.nnModel());
//        State.setCurrentBDS(bds, State.DataType.HISTORIC);
//        State.setImportedBDS(bds); // TODO: live data at first; switch to viewing range

        refreshUIDynamicElements(bds);
    }

    /**
     * Create a thread which sleeps other than every 20 seconds, when it is awake simply to call refresh().
     */
    private void beginRefreshCycle() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                    refresh();
                } catch (Exception e) {
                    priceLabel.setText("PRICE RETRIEVAL ERROR");
                    rsiLabel.setText("RSI RETRIEVAL ERROR");
                }
            }
        }).start();
    }

    private String predictionLabelTitle(BarDataSeries bds) {
        BarAction predBarAction = bds.mostRecentBarAction();
        if (predBarAction == null) return "Unable to predict best move.";
        return "Predicted best move: " + predBarAction.toString();
    }

    private String returnLabelTitle(String returnTitle, double returnPercentage) {
        return returnTitle + " return: " + Util.formatDouble(returnPercentage, 2) + "%";
    }

}
