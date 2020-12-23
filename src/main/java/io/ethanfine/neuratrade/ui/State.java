package io.ethanfine.neuratrade.ui;

import io.ethanfine.neuratrade.Config;
import io.ethanfine.neuratrade.coinbase.CBPublicData;
import io.ethanfine.neuratrade.coinbase.models.CBProduct;
import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;
import io.ethanfine.neuratrade.data.models.BarDataSeries;
import io.ethanfine.neuratrade.neural_network.NNModel;
import org.ta4j.core.BarSeries;

public class State {

    public enum DataType {
        LIVE,
        IMPORTED,
        HISTORIC
    }

    private static DataType currentDataType;

    /**
     * The bar data series that should be displayed in the GUI.
     */
    public static BarDataSeries currentBDS;
    /**
     * The bar data series imported from a file that should be displayed in the GUI. null if historic or live data should
     * be displayed instead.
     */
//    private static BarDataSeries importedBDS;

    static {
        currentBDS = null;
        currentDataType = DataType.LIVE;
//        currentBDS = getRecentBarDataSeries();
//        importedBDS = null;
    }

    /**
     * The bar data series that should be displayed in the GUI.
     * @return bar data series to display in GUI.
     */
//    public static BarDataSeries getDisplayBDS() {
//        if (importedBDS == null) {
//            return getRecentBarDataSeries();
//        } else {
//            return importedBDS;
//        }
//    }
    public static BarDataSeries getDisplayBDS() {
        if (currentDataType == DataType.LIVE) {
            currentBDS = getRecentBarDataSeries();
        }
        return currentBDS;
    }

    /**
     * Whether the bar data series returned by getDisplayBDS() is imported or not.
     * @return true if the bar data series returned by getDisplayBDS() is imported.
     */
//    public static boolean displayBDSisImported() {
//        return importedBDS != null;
//    }

    /**
     * Retrieves the recent bar series for the product in Config, with the chart bar count in Config, and the time
     * granularity in Config.
     * @return Recent bar series with arguments specified in Config if data was successfully retrieved, null
     * otherwise.
     */
    public static BarSeries getRecentBarSeries() {
        return CBPublicData.getRecentBarSeries(
                Config.shared.product,
                Config.shared.chartBarCount.value,
                Config.shared.timeGranularity
        );
    }

    /**
     * Retrieves the recent bar data series for the product in Config, with the chart bar count in Config, and the time
     * granularity in Config.
     * @return Recent bar data series with arguments specified in Config if data was successfully retrieved, null
     * otherwise.
     */
    public static BarDataSeries getRecentBarDataSeries() {
        BarSeries barSeries = getRecentBarSeries();
        if (barSeries == null || barSeries.isEmpty()) {
            return null;
        } else {
            return new BarDataSeries(
                    Config.shared.product,
                    barSeries,
                    Config.shared.timeGranularity,
                    false
            );
        }
    }

    public static DataType getCurrentDataType() {
        return currentDataType;
    }

    /**
     * Sets the bar data series that should be displayed by the GUI to bds.
     * @param bds The bar data series that should be displayed by the GUI. Should be null to specify that the GUI should
     * display the most recent bar data series.
     */
//    public static void setImportedBDS(BarDataSeries bds) {
//        importedBDS = bds;
//    }
    public static void setCurrentBDS(BarDataSeries bds, DataType dataType) {
        currentDataType = dataType;
        currentBDS = bds;
    }

    public static boolean canDisplayPredictions(BarDataSeries bds) {
        return bds.product == CBProduct.BTCUSD &&
                (bds.timeGranularity == CBTimeGranularity.HOUR || bds.timeGranularity == CBTimeGranularity.HOUR_SIX);
    }

    public static boolean shouldDisplayPredictions(BarDataSeries bds) {
        return Config.shared.userSelectedDisplayPredictions && canDisplayPredictions(bds);
    }

}
