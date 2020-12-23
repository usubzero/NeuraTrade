package io.ethanfine.neuratrade.ui.generators;

import ai.djl.Model;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import io.ethanfine.neuratrade.Config;
import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;
import io.ethanfine.neuratrade.data.models.BarAction;
import io.ethanfine.neuratrade.data.models.BarDataSeries;
import io.ethanfine.neuratrade.ui.State;
import io.ethanfine.neuratrade.util.DataSetUtil;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.awt.geom.Ellipse2D;

public class InputsChartGenerator {

    public BarDataSeries barDataSeries;

    public InputsChartGenerator(BarDataSeries barDataSeries) {
        this.barDataSeries = barDataSeries;
    }

    public JFreeChart generateChart() {
        AbstractXYDataset priceDataset = DataSetUtil.createPriceDataSet(barDataSeries);
        XYDataset labelsDataset = DataSetUtil.createBarActionDataset(barDataSeries);
        XYDataset fngDataset = DataSetUtil.createFNGDataset(barDataSeries);
        final CBTimeGranularity timeGranularity = barDataSeries.timeGranularity;

        DateAxis domainAxis = new DateAxis("Date");

        NumberAxis priceRangeAxis = new NumberAxis("Price");
        CandlestickRenderer priceRenderer = new CandlestickRenderer();

        final XYPlot mainPlot = new XYPlot(priceDataset, domainAxis, priceRangeAxis, priceRenderer);
        mainPlot.setDataset(1, labelsDataset);
        XYLineAndShapeRenderer barActionsRenderer = new XYLineAndShapeRenderer(false, true);
        barActionsRenderer.setSeriesPaint(0, Color.BLUE); // labeled buys
        barActionsRenderer.setSeriesPaint(1, Color.ORANGE); // labeled sells
        barActionsRenderer.setSeriesPaint(2, Color.MAGENTA); // predicted sells
        barActionsRenderer.setSeriesPaint(3, Color.CYAN); // predicted buys
        Ellipse2D ellipseSmall = new Ellipse2D.Double(-3.0, -3.0, 6.0, 6.0);
        Ellipse2D ellipseLarge = new Ellipse2D.Double(-6.0, -6.0, 12.0, 12.0);
        barActionsRenderer.setSeriesShape(0, ellipseSmall);
        barActionsRenderer.setSeriesShape(1, ellipseSmall);
        barActionsRenderer.setSeriesShape(2, ellipseLarge);
        barActionsRenderer.setSeriesShape(3, ellipseLarge);
        mainPlot.setRenderer(1, barActionsRenderer);

//        final long ONE_DAY = 24 * 60 * 60 * 1000;
//        XYLineAndShapeRenderer maRenderer = new XYLineAndShapeRenderer(true, false);
//        XYDataset maDataset  = MovingAverage.createMovingAverage(priceDataset, "MA", 200 * ONE_DAY, 0);
//        mainPlot.setRenderer(2, maRenderer);
//        mainPlot.setDataset (2, maDataset);

        priceRenderer.setSeriesPaint(0, Color.BLACK);
        priceRenderer.setDrawVolume(true);
        priceRangeAxis.setAutoRangeIncludesZero(false);

        final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(domainAxis);
        plot.setGap(10);
        plot.add(mainPlot, 5);
//        if (timeGranularity == CBTimeGranularity.DAY) {
//            XYLineAndShapeRenderer fngRenderer = new XYLineAndShapeRenderer();
//            NumberAxis fngRangeAxis = new NumberAxis("FNG Index");
//            XYPlot fngPlot = new XYPlot(fngDataset, domainAxis, fngRangeAxis, fngRenderer);
//            fngRangeAxis.setAutoRangeIncludesZero(false);
//            fngRenderer.setSeriesShape(0, new Rectangle(1, 1));
//            plot.add(fngPlot, 1);
//        }

        final JFreeChart chart = new JFreeChart(
                Config.shared.product.productName,
                null, plot,
                false
        );
        String dataTypeStr = "";
        State.DataType currentDataType = State.getCurrentDataType();
//        switch (currentDataType) {
//            case LIVE: dataTypeStr = "Live";
//            case IMPORTED: dataTypeStr = "Imported";
//            case HISTORIC: dataTypeStr = "Historic";
//        }
        if (currentDataType == State.DataType.LIVE) {
            dataTypeStr = "Live";
        } else if (currentDataType == State.DataType.IMPORTED) {
            dataTypeStr = "Imported";
        } else {
            dataTypeStr = "Historic";
        }
        String chartTitle = barDataSeries.product.productName + " " + dataTypeStr + " Data";
        chart.setTitle(chartTitle);
        return chart;
    }

}
