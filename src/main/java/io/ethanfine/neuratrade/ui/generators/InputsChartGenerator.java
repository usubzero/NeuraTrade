package io.ethanfine.neuratrade.ui.generators;

import io.ethanfine.neuratrade.Config;
import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;
import io.ethanfine.neuratrade.data.models.BarDataSeries;
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
    public XYPlot mainPlot;

    public InputsChartGenerator(BarDataSeries barDataSeries) {
        this.barDataSeries = barDataSeries;
    }

    public JFreeChart generateChart() {
        AbstractXYDataset priceDataset = DataSetUtil.createPriceDataSet(barDataSeries);
        XYDataset labelsDataset = DataSetUtil.createTrainingChartDataset(barDataSeries);
        XYDataset fngDataset = DataSetUtil.createFNGDataset(barDataSeries);
        final CBTimeGranularity timeGranularity = barDataSeries.timeGranularity;

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

        final CombinedDomainXYPlot plot = new CombinedDomainXYPlot(domainAxis);
        plot.setGap(10);
        plot.add(mainPlot, 5);
        if (timeGranularity == CBTimeGranularity.DAY) {
            XYLineAndShapeRenderer fngRenderer = new XYLineAndShapeRenderer();
            NumberAxis fngRangeAxis = new NumberAxis("FNG Index");
            XYPlot fngPlot = new XYPlot(fngDataset, domainAxis, fngRangeAxis, fngRenderer);
            fngRangeAxis.setAutoRangeIncludesZero(false);
            fngRenderer.setSeriesShape(0, new Rectangle(1, 1));
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

}
