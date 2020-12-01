package io.ethanfine.neuratrade.util;

import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class Util {

    /**
     * Convert epoch seconds to an ISO-formatted String of date format yyyy-MM-dd'T'HH:mmXXX.
     * @param epochSeconds the epoch second value to convert.
     * @return an ISO-formatted representation of epochSeconds.
     */
    public static String convertToIsoFromEpoch(long epochSeconds) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmXXX");
        return df.format(new Date(epochSeconds * 1000));
    }

    /**
     * Format a double d to be represented as a String with decimalPlaceCount decimal places displayed.
     * @param d double to create the String representation from.
     * @param decimalPlaceCount the number of decimal places in the String representation of d.
     * @return A String representation of d with decimalPlaceCount decimal places.
     */
    public static String formatDouble(double d, int decimalPlaceCount) {
        String decimalPlaces = new String(new char[decimalPlaceCount]).replace("\0", "#");
        DecimalFormat df = new DecimalFormat("#." + decimalPlaces);
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(d);
    }

    /**
     * Create a deep array from a String that is formatted as a deep array of values that can be converted to doubles.
     * For example, the String "[[3], [1, 4]]" will be converted to the double array [[3], [1, 4]].
     * @param str String that is formatted as a deep array of values that can be converted to doubles to convert.
     * @return a deep array of doubles extrapolated from the representation of the array, str.
     */
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
            Collections.reverse(groupedValues); // Returned from newest to oldest, want reverse order most of the time and Bar

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
