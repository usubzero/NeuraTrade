package io.ethanfine.neuratrade.ui;

import io.ethanfine.neuratrade.Config;
import io.ethanfine.neuratrade.coinbase.CBProduct;
import io.ethanfine.neuratrade.coinbase.CBPublicData;
import io.ethanfine.neuratrade.coinbase.CBTimeGranularity;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UserInterfaceMain implements ActionListener {

    JFrame frame;
    JPanel chartPanel;
    JLabel rsiLabel;
    JComboBox productSelector;

    public UserInterfaceMain() {
        frame = new JFrame("NeuraTrade");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 300);
        frame.setPreferredSize(new Dimension(1000, 500));
        loadFrameContent();
        frame.pack();
        frame.setVisible(true);
    }

    private void loadFrameContent() {
        loadChartPanel();
        loadTickerPriceLabel();
        loadRSILabel();
        loadProductSelector();
//        load();
    }

    private void loadChartPanel() {
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.PAGE_AXIS));
        chartPanel.setPreferredSize(new Dimension(300, 300));
        frame.getContentPane().add(chartPanel, BorderLayout.CENTER);
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
        JLabel label = new JLabel(labelTitle, JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.BOTTOM);
        label.setHorizontalTextPosition(JLabel.CENTER);
        frame.add(label, BorderLayout.NORTH);

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000 / Config.shared.tickerUpdatesPerSecond);
                    String newLabelTitle = "$" + CBPublicData.getTickerPrice(Config.shared.product);
                    label.setText(newLabelTitle);

                    BarSeries recentBarSeries = CBPublicData.getRecentBarSeries(CBProduct.BTCUSD, 300, CBTimeGranularity.MINUTE);
                    ClosePriceIndicator closePrice = new ClosePriceIndicator(recentBarSeries);
                    RSIIndicator rsiIndicator = new RSIIndicator(closePrice, Config.shared.rsiCalculationTickCount);
                    double rsi = rsiIndicator.getValue(299).doubleValue();
                    String rsiLabelTitle = "RSI:" + rsi;
                    rsiLabel.setText(rsiLabelTitle);
                } catch (Exception e) {
                    label.setText("PRICE RETRIEVAL ERROR");
                }
            }
        }).start();
    }

    private void loadRSILabel() {
        double rsi = 0.0;
        boolean errorRetrievingRecentBars = false;
        try {
            BarSeries recentBarSeries = CBPublicData.getRecentBarSeries(CBProduct.BTCUSD, 300, CBTimeGranularity.MINUTE);
            ClosePriceIndicator closePrice = new ClosePriceIndicator(recentBarSeries);
            RSIIndicator rsiIndicator = new RSIIndicator(closePrice, Config.shared.rsiCalculationTickCount);
            rsi = rsiIndicator.getValue(299).doubleValue();
        } catch (Exception e) {
            errorRetrievingRecentBars = true;
        }
        String labelTitle = errorRetrievingRecentBars ? "RSI RETRIEVAL ERROR" : "RSI:" + rsi;
        rsiLabel = new JLabel(labelTitle, JLabel.CENTER);
        rsiLabel.setVerticalTextPosition(JLabel.BOTTOM);
        rsiLabel.setHorizontalTextPosition(JLabel.CENTER);
        frame.add(rsiLabel, BorderLayout.NORTH);
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
            System.out.println(Config.shared.product.productName);
        }
    }
}
