package io.ethanfine.neuratrade.networking;

import io.ethanfine.neuratrade.networking.exceptions.NetworkRequestException;
import io.ethanfine.neuratrade.util.Constants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NetworkingManager {

    /*
    Returns the HTTP response for the request as a String
     */
    public static String performGetRequest(String urlString) throws NetworkRequestException {
        URL url;
        try {
            url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new NetworkRequestException("Malformed URL " + urlString);
        }

        HttpURLConnection conn;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
        } catch (Exception e) {
            throw new NetworkRequestException("Could not open connection with URL " + url.toString());
        }
        conn.addRequestProperty("User-Agent", Constants.CB_USER_AGENT);

        try {
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();

                String readLine;
                while ((readLine = rd.readLine()) != null) {
                    response.append(readLine);
                }
                rd.close();

                return response.toString();
            } else {
                throw new NetworkRequestException("Erroneous response code " + responseCode + " for request with URL " + url.toString());
            }
        } catch (Exception e) {
            throw new NetworkRequestException("IOException occurred when attempting to fulfill request with URL " + url.toString());
        }
    }

}
