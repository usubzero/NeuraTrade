package io.ethanfine.neuratrade.coinbase;

import io.ethanfine.neuratrade.networking.NetworkingManager;
import io.ethanfine.neuratrade.networking.exceptions.NetworkRequestException;
import io.ethanfine.neuratrade.util.Constants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import io.ethanfine.neuratrade.util.Util;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.ta4j.core.*;

public class CBPublicData {

    /*
    Get the epoch time (in seconds) from Coinbase Pro
     */
    public static long getCBTime() throws NetworkRequestException {
        String cbAPIEndPtUrlString = Constants.CB_API_URL + Constants.CB_API_ENDPOINT_TIME;
        String get_response = NetworkingManager.performGetRequest(cbAPIEndPtUrlString);
        try {
            JSONParser parser = new JSONParser();
            JSONObject jObj = (JSONObject) parser.parse(get_response);
            return (long) ((double) jObj.get("epoch"));
        } catch (Exception e) {
            throw new NetworkRequestException("Failed to parse response from request with URL " + cbAPIEndPtUrlString);
        }
    }

    public static BaseBar getTicker(CBProduct product) throws Exception {
        BaseBar tickerBar;

        URL cb_api_url = new URL(Constants.CB_API_URL + Constants.CB_API_ENDPOINT_TICKER(product));
        HttpURLConnection conn = (HttpURLConnection) cb_api_url.openConnection();
        conn.setRequestMethod("GET");
        conn.addRequestProperty("User-Agent", Constants.CB_USER_AGENT);
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();

            String readLine;
            while ((readLine = rd.readLine()) != null) {
                response.append(readLine);
            }
            rd.close();

            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(response.toString());
            tickerBar = null;
            // TODO: instantiate tickerBar based off JSON
        } else {
            throw new Exception("Erroneous response code: " + responseCode);
        }

        return tickerBar;
    }

    public static BarSeries getBarSeries(CBProduct product, long startTime, long endTime, CBTimeGranularity timeGranularity) throws NetworkRequestException {
        // Cap the number of bars to 300 due to API restrictions
        if (((endTime - startTime) / timeGranularity.seconds) > 300) {
            endTime = startTime + timeGranularity.seconds * 300;
        }

        BarSeries barSeries = new BaseBarSeriesBuilder().withName(product.productName).build();
        String cbAPIEndPtUrlString = Constants.CB_API_URL + Constants.CB_API_ENDPOINT_HISTORIC_RATES(product, startTime, endTime, timeGranularity);
        String get_response = NetworkingManager.performGetRequest(cbAPIEndPtUrlString);
        double[][] bars = Util.stringToDeep(get_response);
        for (double[] bar : bars) {
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond((long) bar[0]), ZoneId.systemDefault());
            zonedDateTime = zonedDateTime.plusDays(1); // TODO: base on granularity

            try {
                barSeries.addBar(zonedDateTime, bar[3], bar[2], bar[1], bar[4], bar[5]);
            } catch (Exception e) {
                // NOTHING; TODO: find out why exception is being generated here
            }
        }

        return barSeries;
    }

    public static BarSeries getBarSeries(CBProduct product, long startTime, int barCount, CBTimeGranularity timeGranularity) throws Exception {
        long endTime = startTime + barCount * timeGranularity.seconds;
        return getBarSeries(product, startTime, endTime, timeGranularity);
    }

    public static BarSeries getBarSeries(CBProduct product, int barCount, long endTime, CBTimeGranularity timeGranularity) throws Exception {
        long startTime = endTime - barCount * timeGranularity.seconds;
        return getBarSeries(product, startTime, endTime, timeGranularity);
    }

    public static BarSeries getRecentBarSeries(CBProduct product, int barCount, CBTimeGranularity timeGranularity) throws Exception {
        long endTime = getCBTime();
        return getBarSeries(product, barCount, endTime, timeGranularity);
    }

}
