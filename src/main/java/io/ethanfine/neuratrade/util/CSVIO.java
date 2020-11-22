package io.ethanfine.neuratrade.util;

import io.ethanfine.neuratrade.coinbase.models.CBProduct;
import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;
import io.ethanfine.neuratrade.data.models.BarAction;
import io.ethanfine.neuratrade.data.models.BarDataPoint;
import io.ethanfine.neuratrade.data.models.BarDataSeries;
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
            String[] productNameSplit = fileName.split(",");
            CBProduct product = CBProduct.from(productNameSplit[0]);
            String[] timeGranularitySplit = productNameSplit[1].split("\\.");
            boolean proprietaryFileFormat = true;
            int timeGranularitySeconds = Integer.parseInt(timeGranularitySplit[0]);
            CBTimeGranularity timeGranularity = CBTimeGranularity.from(timeGranularitySeconds);

            String line = "";
            int i = 0;
            BarSeries barSeries = new BaseBarSeriesBuilder().withName(product.productName).build();
            ArrayList<Pair<Double[], BarAction>> bdsSupplementalData = new ArrayList<>();
            while ((line = bufferedReader.readLine()) != null) {
                i++;
                if (i == 1) {
                    continue;
                }
                String[] rawDataPoint = line.split(",");
                long epochSeconds = Long.parseLong(rawDataPoint[0]);
                ZonedDateTime zdtFromEpoch = ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSeconds), ZoneId.systemDefault());
                zdtFromEpoch = zdtFromEpoch.plusDays(1);
                // TODO: remove above line; epochSeconds is correct, conversion isn't
                double open = Double.parseDouble(rawDataPoint[1]);
                double high = Double.parseDouble(rawDataPoint[2]);
                double low = Double.parseDouble(rawDataPoint[3]);
                // TODO: fix that low and close appear to be the same
                double close = Double.parseDouble(rawDataPoint[4]);
                double rsi = Double.parseDouble(rawDataPoint[5]);
                double sma20 = Double.parseDouble(rawDataPoint[6]);
                double sma50 = Double.parseDouble(rawDataPoint[7]);
                double sma200 = Double.parseDouble(rawDataPoint[8]);
                double volume = Double.parseDouble(rawDataPoint[9]);
                double macd;
                BarAction barAction = null;
                if (proprietaryFileFormat) {
                    macd = Double.parseDouble(rawDataPoint[10]);
                    barAction = BarAction.from(rawDataPoint[11]);
                } else {
                    macd = Double.parseDouble(rawDataPoint[11]);
                }
                // TODO: parse more supplemental data
                Double[] supplementalDoubleData = {rsi, sma20, sma50, sma200, (double) epochSeconds, macd};
                System.out.println(supplementalDoubleData);
                Pair<Double[], BarAction> supplementalData = new Pair<>(supplementalDoubleData, barAction);
                bdsSupplementalData.add(supplementalData);
                barSeries.addBar(zdtFromEpoch, open, high, low, close, volume);
            }

            BarDataSeries bds = new BarDataSeries(product, barSeries, timeGranularity);
            for (int bdsI = 0; bdsI < bds.getBarCount(); bdsI++) {
                Pair<Double[], BarAction> sdI = bdsSupplementalData.get(bdsI);
                Double[] sddI = sdI.getKey();
                BarDataPoint bdpI = bds.getBarDataPoint(bdsI);
                bdpI.rsi = sddI[0];
                bdpI.sma20 = sddI[1];
                bdpI.sma50 = sddI[2];
                bdpI.sma200 = sddI[3];
                bdpI.epochDebugTODORM = sddI[4];
                bdpI.macd = sddI[5];
                BarAction baI = sdI.getValue();
                if (baI != null) {
                    bdpI.barAction = baI;
                }
            }
            return bds;
        } catch (Exception e) {
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
               fw.write("time,open,high,low,close,RSI,SMA20,SMA50,SMA200,Volume,MACD,BarAction\n");
               for (int i = 0; i < bds.getBarCount(); i++) {
                   BarDataPoint bdpI = bds.getBarDataPoint(i);
                   fw.write(bdpI.bar.getBeginTime().toEpochSecond() + "," + bdpI.bar.getOpenPrice() + "," + bdpI.bar.getHighPrice() + "," + bdpI.bar.getLowPrice() + "," + bdpI.bar.getClosePrice() + "," + bdpI.rsi + "," + bdpI.sma20 + "," + bdpI.sma50 + ","  + bdpI.sma200 + ","  + bdpI.bar.getVolume() + ","  + bdpI.macd + ","  + bdpI.barAction.stringRep + "\n");
               }
               fw.flush();
               fw.close();
               // TODO: check why not all BDPs appear to be written
           } else {
               System.out.println("File already exists at " + filePath);
           }
       } catch (IOException e) {
           System.out.println("Failed to write CSV file at path: " + e.getMessage());
       }
    }

}
