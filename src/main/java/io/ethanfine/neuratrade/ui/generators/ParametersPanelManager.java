package io.ethanfine.neuratrade.ui.generators;

import io.ethanfine.neuratrade.Config;
import io.ethanfine.neuratrade.coinbase.models.CBProduct;
import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;
import io.ethanfine.neuratrade.data.models.BarDataSeries;
import io.ethanfine.neuratrade.ui.State;
import io.ethanfine.neuratrade.ui.UIMain;
import io.ethanfine.neuratrade.ui.models.ChartBarCount;
import io.ethanfine.neuratrade.util.CSVIO;
import org.jdesktop.swingx.JXDatePicker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class ParametersPanelManager implements ActionListener {

    UIMain ui;
    JPanel parametersPanel;
    JButton toggleLiveHistoricButton;
    public JCheckBox predictionsToggler;
    JComboBox productSelector;
    JComboBox barCountSelector;
    JComboBox granularitySelector;
    JXDatePicker startDatePicker;
    JXDatePicker endDatePicker;

    /**
     * Instantiates a new parameters panel to be managed.
     * @param ui the UIMain instance that the parameter panel will be used in.
     */
    public ParametersPanelManager(UIMain ui) {
        this.ui = ui;
        loadParametersPanel();
    }

    /**
     * @return Parameters panel.
     */
    public JPanel getPanel() {
        return this.parametersPanel;
    }

    /**
     * Instantiates a JPanel that allows for interactions with the Config parameters of product, bar count, and
     * time granularity. Adds such panel to the frame and all parameter JComboBox selectors to the panel.
     */
    private void loadParametersPanel() {
        parametersPanel = new JPanel();
        parametersPanel.setPreferredSize(new Dimension(400, 40));
        ui.frame.getContentPane().add(parametersPanel, BorderLayout.SOUTH);
        predictionsToggler = new JCheckBox("Display Predictions");
        predictionsToggler.addActionListener(this);
        parametersPanel.add(predictionsToggler);
        productSelector = loadParametersPanelSelector(CBProduct.values(), Config.shared.product);
        parametersPanel.add(productSelector);
        barCountSelector = loadParametersPanelSelector(ChartBarCount.values(), Config.shared.chartBarCount);
        parametersPanel.add(barCountSelector);
        granularitySelector = loadParametersPanelSelector(CBTimeGranularity.values(), Config.shared.timeGranularity);
        parametersPanel.add(granularitySelector);
        startDatePicker = new JXDatePicker();
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.add(Calendar.MONTH, -1);
        startDatePicker.setDate(startCalendar.getTime());
        startDatePicker.setFormats(new SimpleDateFormat("MM/dd/yyyy"));
        startDatePicker.setVisible(false);
        startDatePicker.addActionListener(this);
        parametersPanel.add(startDatePicker);
        endDatePicker = new JXDatePicker();
        endDatePicker.setDate(Calendar.getInstance().getTime()); // TODO: max for data
        endDatePicker.setFormats(new SimpleDateFormat("MM/dd/yyyy"));
        endDatePicker.setVisible(false);
        endDatePicker.addActionListener(this);
        parametersPanel.add(endDatePicker);
        toggleLiveHistoricButton = new JButton("Switch to historic BTC data");
        toggleLiveHistoricButton.addActionListener(this);
        parametersPanel.add(toggleLiveHistoricButton, BorderLayout.CENTER);
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

    // TODO: doc
    public void refreshParametersPanel() {
        State.DataType currentDataType = State.getCurrentDataType();
        if (currentDataType == State.DataType.LIVE) {
            productSelector.setVisible(true);
            barCountSelector.setVisible(true);
            granularitySelector.setVisible(true);
            startDatePicker.setVisible(false);
            endDatePicker.setVisible(false);
            toggleLiveHistoricButton.setText("Switch to historic BTC data");
        } else if (currentDataType == State.DataType.IMPORTED) {
            productSelector.setVisible(false);
            barCountSelector.setVisible(false);
            granularitySelector.setVisible(false);
            startDatePicker.setVisible(false);
            endDatePicker.setVisible(false);
            toggleLiveHistoricButton.setText("Switch to live data");
        } else {
            productSelector.setVisible(false);
            barCountSelector.setVisible(false);
            granularitySelector.setVisible(true);
            startDatePicker.setVisible(true);
            endDatePicker.setVisible(true);
            toggleLiveHistoricButton.setText("Switch to live data");
        }
//        if (State.displayBDSisImported()) {
//            productSelector.setVisible(false);
//            barCountSelector.setVisible(false);
//            granularitySelector.setVisible(false);
//            switchToLiveButton.setVisible(true);
//        } else {
//            switchToLiveButton.setVisible(false);
//            productSelector.setVisible(true);
//            barCountSelector.setVisible(true);
//            granularitySelector.setVisible(true);
//        }
    }

    /**
     * Detect actions on the chart panel.
     * @param e the action that was performed on the panel.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == predictionsToggler) {
            Config.shared.userSelectedDisplayPredictions = predictionsToggler.isSelected();
            if (State.getCurrentDataType() == State.DataType.HISTORIC) updateHistoricDataDisplayed();
        } else if (e.getSource() == productSelector) {
            Config.shared.product = (CBProduct) productSelector.getSelectedItem();
            if (State.getCurrentDataType() == State.DataType.HISTORIC) updateHistoricDataDisplayed();
        } else if (e.getSource() == barCountSelector) {
            Config.shared.chartBarCount = (ChartBarCount) barCountSelector.getSelectedItem();
            if (State.getCurrentDataType() == State.DataType.HISTORIC) updateHistoricDataDisplayed();
        } else if (e.getSource() == granularitySelector) {
            Config.shared.timeGranularity = (CBTimeGranularity) granularitySelector.getSelectedItem();
            if (State.getCurrentDataType() == State.DataType.HISTORIC) updateHistoricDataDisplayed();
        } else if (e.getSource() == toggleLiveHistoricButton) {
            if (State.getCurrentDataType() == State.DataType.LIVE) {
                updateHistoricDataDisplayed();
            } else {
                State.setCurrentBDS(null, State.DataType.LIVE);
            }
        } else if (e.getSource() == startDatePicker || e.getSource() == endDatePicker) {
            if (State.getCurrentDataType() == State.DataType.HISTORIC) updateHistoricDataDisplayed();
        }
//        } else if (e.getSource() == endDatePicker) {
//            Date startPickerDate = (Date) startDatePicker.getEditor().getValue();
//            long startDate = startPickerDate.toInstant().getEpochSecond();
//            Date endPickerDate = (Date) endDatePicker.getEditor().getValue();
//            long endDate = endPickerDate.toInstant().getEpochSecond();
//            CBTimeGranularity tg = Config.shared.timeGranularity;
//            BarDataSeries bds = CSVIO.readFile("historic_data/BTC-USD," + tg.seconds +".csv", startDate, endDate);
//            if (bds != null) {
//                Config.shared.product = CBProduct.BTCUSD;
//                bds.labelTradePredictions(bds.timeGranularity.nnModel());
//                State.setCurrentBDS(bds, State.DataType.HISTORIC);
//            } else {
//                State.setCurrentBDS(null, State.DataType.LIVE); // TODO: handle
//            }
//        }
        // TODO: reset to previous value if request for new data fails on live data selectors

        ui.refresh();
    }

    private void updateHistoricDataDisplayed() {
        Date startPickerDate = (Date) startDatePicker.getEditor().getValue();
        long startDate = startPickerDate.toInstant().getEpochSecond();
        Date endPickerDate = (Date) endDatePicker.getEditor().getValue();
        long endDate = endPickerDate.toInstant().getEpochSecond();
        CBTimeGranularity tg = Config.shared.timeGranularity;
        BarDataSeries bds = CSVIO.readFile("historic_data/BTC-USD," + tg.seconds +".csv", startDate, endDate);
        if (bds != null) {
            Config.shared.product = CBProduct.BTCUSD;
            bds.labelTradePredictions(bds.timeGranularity.nnModel());
            State.setCurrentBDS(bds, State.DataType.HISTORIC);
        } else {
            State.setCurrentBDS(null, State.DataType.LIVE); // TODO: handle
        }
    }

}
