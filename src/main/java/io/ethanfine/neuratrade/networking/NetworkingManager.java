package io.ethanfine.neuratrade.networking;

import io.ethanfine.neuratrade.networking.exceptions.NetworkRequestException;
import io.ethanfine.neuratrade.util.Constants;
import org.riversun.promise.Func;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NetworkingManager {

    /**
     * Executes a GET request from the URL represented by urlString. Then a Func representation of such
     * request is formed so that this request can be resolved within a Promise.
     * @param urlString The URL to which the GET request should be made.
     */
    public static Func performGetRequest(String urlString) {
        return (action, data) -> {
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

                    action.resolve(response.toString());
                } else {
                    throw new NetworkRequestException(
                            "Erroneous response code " + responseCode + " for request with URL " + url.toString()
                    );
                }
            } catch (Exception e) {
                throw new NetworkRequestException(
                        "IOException occurred when attempting to fulfill request with URL " + url.toString()
                );
            }
        };
    }

}
