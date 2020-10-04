package io.ethanfine.neuratrade.util;

import io.ethanfine.neuratrade.data.models.BarDataSeries;

import java.io.BufferedReader;
import java.io.FileReader;

public class TradingViewCSVReader {

    /**
     * Read bar data from a CSV file at filePath and create a BarDataSeries from such data.
     * TODO: continue implementation
     * @param filePath the path of the file to read bar data from.
     * @return a BarDataSeries created from the bar data at a file located at filePath.
     */
    public static BarDataSeries readFile(String filePath) {
        BufferedReader bufferedReader;
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));

            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                String[] rawDataPoint = line.split(",");

            }
        } catch (Exception e) {
            System.out.println("Failed to read CSV file at path: " + filePath);
        }
        return null;
    }

}
