package io.ethanfine.neuratrade.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Util {

    public static String convertToIsoFromEpoch(long epochSeconds) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX");
        return df.format(new Date(epochSeconds * 1000));
    }

    public static double[][] stringToDeep(String str) {
        try {
            str = str.substring(2, str.length() - 3);
            int innerArraysLen = 0;
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == ',') {
                    innerArraysLen++;
                } else if (str.charAt(i) == ']') {
                    innerArraysLen++;
                    break;
                }
            }
            str = str.replaceAll("]", "");
            str = str.replaceAll("\\[", "");

            String vals[] = str.split(",");
            ArrayList<double[]> groupedValues = new ArrayList<>();
            for (int valI = 0; valI < vals.length; valI += innerArraysLen) {
                double[] innerArrayVals = new double[innerArraysLen];
                for (int innerArrayValI = 0; innerArrayValI < innerArraysLen; innerArrayValI++) {
                    innerArrayVals[innerArrayValI] = Double.parseDouble(vals[valI + innerArrayValI]);
                }
                groupedValues.add(innerArrayVals);
            }

            double[][] deep = new double[groupedValues.size()][innerArraysLen];
            for (int i = 0; i < deep.length; i++) {
                deep[i] = groupedValues.get(i);
            }

            return deep;
        } catch (Exception e) {
            System.out.println("Failed to parse string representation of deep array"); // TODO: log
            return new double[0][0];
        }
    }

}
