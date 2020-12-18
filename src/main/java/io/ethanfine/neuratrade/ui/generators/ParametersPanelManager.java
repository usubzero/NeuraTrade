package io.ethanfine.neuratrade.ui.generators;

import io.ethanfine.neuratrade.Config;
import io.ethanfine.neuratrade.coinbase.models.CBProduct;
import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;
import io.ethanfine.neuratrade.ui.State;
import io.ethanfine.neuratrade.ui.UIMain;
import io.ethanfine.neuratrade.ui.models.ChartBarCount;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class ParametersPanelManager implements ActionListener {

    UIMain ui;
    JPanel parametersPanel;
    JButton switchToLiveButton;
    public JCheckBox predictionsToggler;
    JComboBox productSelector;
    JComboBox barCountSelector;
    JComboBox granularitySelector;

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
        switchToLiveButton = new JButton("Switch to live data");
        switchToLiveButton.addActionListener(this);
        switchToLiveButton.setVisible(false);
        parametersPanel.add(switchToLiveButton, BorderLayout.CENTER);
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
        if (State.displayBDSisImported()) {
            productSelector.setVisible(false);
            barCountSelector.setVisible(false);
            granularitySelector.setVisible(false);
            switchToLiveButton.setVisible(true);
        } else {
            switchToLiveButton.setVisible(false);
            productSelector.setVisible(true);
            barCountSelector.setVisible(true);
            granularitySelector.setVisible(true);
        }
    }

    /**
     * Detect actions on the chart panel.
     * @param e the action that was performed on the panel.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == predictionsToggler) {
            Config.shared.userSelectedDisplayPredictions = predictionsToggler.isSelected();
        } else if (e.getSource() == productSelector) {
            Config.shared.product = (CBProduct) productSelector.getSelectedItem();
        } else if (e.getSource() == barCountSelector) {
            Config.shared.chartBarCount = (ChartBarCount) barCountSelector.getSelectedItem();
        } else if (e.getSource() == granularitySelector) {
            Config.shared.timeGranularity = (CBTimeGranularity) granularitySelector.getSelectedItem();
        } else if (e.getSource() == switchToLiveButton) {
            State.setImportedBDS(null);
        }
        // TODO: reset to previous value if request for new data fails on live data selectors

        ui.refresh();
    }

}
