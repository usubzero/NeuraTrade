package io.ethanfine.neuratrade.coinbase;

import io.ethanfine.neuratrade.util.Constants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.ta4j.core.*;

public class CBPublicData {

    public static BaseBar getBTCTicker() throws Exception {
        BaseBar tickerBar;
        URL cb_api_url = new URL(Constants.CB_API_URL + Constants.CB_API_ENDPOINT_TICKER("BTC-USD"));
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

}
