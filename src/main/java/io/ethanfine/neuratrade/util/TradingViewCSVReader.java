package io.ethanfine.neuratrade.util;

import java.io.BufferedReader;
import java.io.FileReader;

public class TradingViewCSVReader {

    public static void readFile(String filePath) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));

            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                String[] rawDataPoint = line.split(",");

            }
        } catch (Exception e) {
            System.out.println("Failed to read CSV file at path: " + filePath);
        }
    }

}
