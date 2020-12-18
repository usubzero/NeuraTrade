package io.ethanfine.neuratrade.util;

import ai.djl.translate.TranslateException;
import io.ethanfine.neuratrade.Config;
import io.ethanfine.neuratrade.coinbase.models.CBProduct;
import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;
import io.ethanfine.neuratrade.data.models.BarAction;
import io.ethanfine.neuratrade.data.models.BarDataPoint;
import io.ethanfine.neuratrade.data.models.BarDataSeries;
import io.ethanfine.neuratrade.data.models.Trade;
import io.ethanfine.neuratrade.neural_network.NNModel;
import javafx.util.Pair;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;

import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class CSVIO {

    /**
     * Read bar data from a CSV file at filePath and create a BarDataSeries from such data.
     * Requires: TODO
     * @param filePath the path of the file to read bar data from.
     * @return a BarDataSeries created from the bar data at a file located at filePath.
     */
    public static BarDataSeries readFile(String filePath) {
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));

            String[] filePathDirSplit = filePath.split("/");
            String fileName = filePathDirSplit[filePathDirSplit.length - 1];
            String[] metaDataSplit = fileName.split("\\.");
            String[] metaData = metaDataSplit[0].split(",");
            CBProduct product = CBProduct.from(metaData[0]);
            int timeGranularitySeconds = Integer.parseInt(metaData[1]);
            CBTimeGranularity timeGranularity = CBTimeGranularity.from(timeGranularitySeconds);
            boolean labeledFileFormat = false;
            boolean predictedFileFormat = false;
            if (metaData.length > 2) {
                if (metaData[2].equals("TDATA"))
                    labeledFileFormat = true;
                if (metaData[2].equals("PDATA"))
                    predictedFileFormat = true;
            }

            String line = "";
            int i = 0;
            BarSeries barSeries = new BaseBarSeriesBuilder().withName(product.productName).build();
            ArrayList<Pair<Double[], Pair<BarAction, BarAction>>> bdsSupplementalData = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null) {
                i++;
                if (i == 1) {
                    continue;
                }
                String[] rawDataPoint = line.split(",");
                long epochSeconds = Long.parseLong(rawDataPoint[0]);
                ZonedDateTime zdtFromEpoch = ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
                zdtFromEpoch = zdtFromEpoch.plusDays(1);
                // TODO: check above line; epochSeconds is correct, conversion might not be
                double open = Double.parseDouble(rawDataPoint[1]);
                double high = Double.parseDouble(rawDataPoint[2]);
                double low = Double.parseDouble(rawDataPoint[3]);
                double close = Double.parseDouble(rawDataPoint[4]);
                double rsi = Double.parseDouble(rawDataPoint[5]);
                double basisOfBB = Double.parseDouble(rawDataPoint[6]);
                double upperOfBB = Double.parseDouble(rawDataPoint[7]);
                double lowerOfBB = Double.parseDouble(rawDataPoint[8]);
                double sma20 = Double.parseDouble(rawDataPoint[9]);
                double sma50 = Double.parseDouble(rawDataPoint[10]);
                double sma200 = Double.parseDouble(rawDataPoint[11]);
                double volume = Double.parseDouble(rawDataPoint[12]);
                double macd;
                double widthOfBB;
                BarAction labelBarAction = null;
                BarAction predictedBarAction = null;
                if (labeledFileFormat || predictedFileFormat) {
                    macd = Double.parseDouble(rawDataPoint[13]);
                    widthOfBB = Double.parseDouble(rawDataPoint[14]);
                    labelBarAction = BarAction.from(rawDataPoint[15]);
                    if (predictedFileFormat)
                        predictedBarAction = BarAction.from(rawDataPoint[16]);
                } else {
                    macd = Double.parseDouble(rawDataPoint[14]);
                    widthOfBB = Double.parseDouble(rawDataPoint[17]);
                }
                Double[] supplementalDoubleData = {rsi, basisOfBB, upperOfBB, lowerOfBB, sma20, sma50, sma200, macd, widthOfBB, (double) epochSeconds};
                Pair<Double[], Pair<BarAction, BarAction>> supplementalData = new Pair<>(supplementalDoubleData, new Pair<>(labelBarAction, predictedBarAction));
                bdsSupplementalData.add(supplementalData);
                try {
                    barSeries.addBar(zdtFromEpoch, open, high, low, close, volume);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }

            int nnImportCorrect = 0;
            BarDataSeries bds = new BarDataSeries(product, barSeries, timeGranularity);
            for (int bdsI = 0; bdsI < bds.getBarCount(); bdsI++) {
                Pair<Double[], Pair<BarAction, BarAction>> sdI = bdsSupplementalData.get(bdsI);
                Double[] sddI = sdI.getKey();
                BarDataPoint bdpI = bds.getBarDataPoint(bdsI);
                bdpI.rsi = sddI[0];
                bdpI.basisOfBB = sddI[1];
                bdpI.upperOfBB = sddI[2];
                bdpI.lowerOfBB = sddI[3];
                bdpI.sma20 = sddI[4];
                bdpI.sma50 = sddI[5];
                bdpI.sma200 = sddI[6];
                bdpI.macd = sddI[7];
                bdpI.widthOfBB = sddI[8];
                bdpI.epochDebugTODORM = sddI[9];
                Pair<BarAction, BarAction> basI = sdI.getValue();
                BarAction labeledBarAction = basI.getKey();
                BarAction predictedBarAction = basI.getValue();
                if (labeledBarAction != null)
                    bdpI.barActionLabeled = labeledBarAction;
                if (predictedBarAction != null)
                    bdpI.tradesPredicted.add(
                            new Trade(bdpI.epochDebugTODORM,
                                    bdpI.bar.getClosePrice().doubleValue(),
                                    predictedBarAction
                            )
                    ); // TODO: intermediate trades
                try {
                    NNModel model = bds.timeGranularity.nnModel();
                    if (model == null) return bds;
                    String pyTorchPred = "";
                    if (predictedBarAction != null) {
                        pyTorchPred = ", Predicted (PyTorch): " + predictedBarAction;
                        if (model.predict(bdpI.neuralNetworkInputs()) == predictedBarAction)
                            nnImportCorrect++;
                    }
                    System.out.println("Predicted (DJL): " + model.predict(bdpI.neuralNetworkInputs()) + pyTorchPred);
                } catch (Exception e)  {
                    System.out.println(e.getMessage());
                }
            }
            System.out.println("DJL accuracy: " + nnImportCorrect + " / " + bds.getBarCount());

            return bds;
        } catch (IOException e) {
            System.out.println("Failed to read CSV file at path: " + e.getMessage());
        }
        return null;
    }

    // TODO: doc
    public static void writeBarDataSeriesToFile(BarDataSeries bds, String filePath) {
       try {
           File file = new File(filePath);
           if (file.createNewFile()) {
               FileWriter fw = new FileWriter(filePath);
               fw.write("time,open,high,low,close,RSI,basisOfBB,upperOfBB,lowerOfBB,SMA20,SMA50,SMA200,Volume,MACD,widthOfBB,BarAction\n");
               System.out.println("BDS bar count: " + bds.getBarCount());
               for (int i = 0; i < bds.getBarCount(); i++) {
                   BarDataPoint bdpI = bds.getBarDataPoint(i);
//                   String predictedBarActionStrRep = bdpI.barActionPredicted == null ? "" : "," + bdpI.barActionPredicted.stringRep;
                   String predictedBarActionStrRep = bdpI.tradesPredicted.isEmpty() ? "" : bdpI.tradesPredicted.get(bdpI.tradesPredicted.size() - 1).barAction.stringRep;
                   fw.write(bdpI.bar.getBeginTime().toEpochSecond() + "," + bdpI.bar.getOpenPrice() + "," + bdpI.bar.getHighPrice() + "," + bdpI.bar.getLowPrice() + "," + bdpI.bar.getClosePrice() + "," + bdpI.rsi + "," + bdpI.basisOfBB + "," + bdpI.upperOfBB + "," + bdpI.lowerOfBB + "," + bdpI.sma20 + "," + bdpI.sma50 + ","  + bdpI.sma200 + ","  + bdpI.bar.getVolume() + ","  + bdpI.macd + "," + bdpI.widthOfBB + ","  + bdpI.barActionLabeled.stringRep + predictedBarActionStrRep + "\n");
               }
               fw.flush();
               fw.close();
           } else {
               System.out.println("File already exists at " + filePath);
           }
       } catch (IOException e) {
           System.out.println("Failed to write CSV file at path: " + e.getMessage());
       }
    }

}
