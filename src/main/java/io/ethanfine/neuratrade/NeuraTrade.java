package io.ethanfine.neuratrade;

import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;
import io.ethanfine.neuratrade.data.models.BarDataSeries;
import io.ethanfine.neuratrade.neural_network.NNModel;
import io.ethanfine.neuratrade.ui.UIMain;
import io.ethanfine.neuratrade.util.CSVIO;
import io.ethanfine.neuratrade.util.Util;
import javafx.util.Pair;

import java.util.Random;

public class NeuraTrade {

    /**
     * Creates a new UIMain instance for the app.
     * @param args Unused
     */
    public static void main(String args[]) {
        new UIMain();
        printReturnsRandomPeriods();
    }

    private static void printReturnsRandomPeriods() {
        System.out.println("Calculating returns over random periods...");
        Pair<Double, Double> returnsSixHourTGPosBasis = calculateReturnsRandomPeriods(5, 10, CBTimeGranularity.HOUR_SIX, basisReturn -> basisReturn > 0);
        Pair<Double, Double> returnsSixHourTGNegBasis = calculateReturnsRandomPeriods(5, 10, CBTimeGranularity.HOUR_SIX, basisReturn -> basisReturn < 0);
        Pair<Double, Double> returnsSixHourTGNearZeroBasis = calculateReturnsRandomPeriods(5, 10, CBTimeGranularity.HOUR_SIX, basisReturn -> basisReturn < 2 && basisReturn > -2);
//        Pair<Double, Double> returnsHourTG = calculateReturnsRandomPeriods(5, 5, CBTimeGranularity.HOUR);
        System.out.println("================ Random Period Return Calculations Summary ==================");
        if (returnsSixHourTGPosBasis != null) {
            System.out.println("Basis Return Sampling Mean (HOUR_SIX time granularity, + BASIS): " + returnsSixHourTGPosBasis.getKey() + "%");
            System.out.println("Predictive Return Sampling Mean (HOUR_SIX time granularity, + BASIS): " + returnsSixHourTGPosBasis.getValue() + "%");
        }
        if (returnsSixHourTGNegBasis != null) {
            System.out.println("Basis Return Sampling Mean (HOUR_SIX time granularity, - BASIS): " + returnsSixHourTGNegBasis.getKey() + "%");
            System.out.println("Predictive Return Sampling Mean (HOUR_SIX time granularity, - BASIS): " + returnsSixHourTGNegBasis.getValue() + "%");
        }
        if (returnsSixHourTGNearZeroBasis != null) {
            System.out.println("Basis Return Sampling Mean (HOUR_SIX time granularity, ~0 BASIS): " + returnsSixHourTGNearZeroBasis.getKey() + "%");
            System.out.println("Predictive Return Sampling Mean (HOUR_SIX time granularity, ~0 BASIS): " + returnsSixHourTGNearZeroBasis.getValue() + "%");
        }
//        if (returnsHourTG != null) {
//            System.out.println("Basis Return Sampling Mean (HOUR time granularity): " + returnsHourTG.getKey() + "%");
//            System.out.println("Predictive Return Sampling Mean (HOUR time granularity): " + returnsHourTG.getValue() + "%");
//        }
    }

    private static Pair<Double, Double> calculateReturnsRandomPeriods(int numSamples, int sampleSize, CBTimeGranularity timeGranularity, java.util.function.Predicate<? super Double> basisReturnPredicate) {
        long minEpoch = 1463637600;
        long maxEpoch = 1552114800;
        NNModel predictionsModel = timeGranularity.nnModel();
        if (predictionsModel == null) {
            System.out.println("Can't calculate predictive returns for " + timeGranularity + " time granularity. No prediction model is available for this time granularity.");
            return null;
        }
        if (timeGranularity == CBTimeGranularity.HOUR) {
            minEpoch = 1552114800;
            maxEpoch = 1598918400;
        }
        /* 1598918400 - 1604213100 is the epoch range of the 1h model training data
           1552114800 - 1606345200 is the epoch range of the 6h model training data
           We don't want to include periods which are in the range of their time granularity's model training data when evaluating returns */

        double cumBasisReturnMeans = 0;
        double cumPredictiveReturnMeans = 0;
        for (int o = 0; o < numSamples; o++) {
            int i = 0;
            double cumBasisReturnSample = 0;
            double cumPredictiveReturnSample = 0;
            while (i < sampleSize) {
                Random rand = new Random();
                int random_start = rand.nextInt((int) (maxEpoch - minEpoch)) + (int) minEpoch;
                int random_end = rand.nextInt(((int) maxEpoch) - random_start) + random_start;
                int tgSeconds = timeGranularity.seconds;
                if (random_end - random_start < tgSeconds * 10) continue;
                String tgDataFilePath = "historic_data/BTC-USD," + tgSeconds + ".csv";
                BarDataSeries bds = CSVIO.readFile(tgDataFilePath, random_start, random_end);
                if (bds == null) {
                    System.out.println("Couldn't find data for time granularity " + timeGranularity + ". This is necessary to calculate returns over random periods.");
                    break;
                }
                bds.labelTradePredictions(predictionsModel);
                double basisReturn = bds.basisReturn();
                if (!basisReturnPredicate.test(basisReturn)) continue;
//                if (basisReturn > 0) continue; // TODO: make condition on basis return parametrizable
                i++;
                double predictiveReturn = bds.predictionsReturn();
                cumBasisReturnSample += basisReturn;
                cumPredictiveReturnSample += predictiveReturn;
                System.out.println("Basis return over random period #" + i + " in sample #" + (o + 1) + ": " + basisReturn + "% for date range: " + Util.convertToIsoFromEpoch(random_start) + " through " + Util.convertToIsoFromEpoch(random_end) + " and time granularity " + timeGranularity);
                System.out.println("Predictive return over random period #" + i + " in sample #" + (o + 1) + ": " + predictiveReturn + "%");
            }
            double basisReturnSampleMean = cumBasisReturnSample / sampleSize;
            double predictiveReturnSampleMean = cumPredictiveReturnSample / sampleSize;
            cumBasisReturnMeans += basisReturnSampleMean;
            cumPredictiveReturnMeans += predictiveReturnSampleMean;
            System.out.println("Basis Return Sample #" + (o + 1)  + " Mean: " + basisReturnSampleMean + "%");
            System.out.println("Predictive Return Sample #" + (o + 1)  + " Mean: " + predictiveReturnSampleMean + "%");
        }
        double cumBasisReturnSamplingMean = cumBasisReturnMeans / numSamples;
        double cumPredictiveReturnSamplingMean = cumPredictiveReturnMeans / numSamples;
        System.out.println("Basis Return Sampling Mean (" + timeGranularity + " time granularity): " + cumBasisReturnSamplingMean + "%");
        System.out.println("Predictive Return Sampling Mean (" + timeGranularity + " time granularity): " + cumPredictiveReturnSamplingMean + "%");
        return new Pair<>(cumBasisReturnSamplingMean, cumPredictiveReturnSamplingMean);
    }

}
