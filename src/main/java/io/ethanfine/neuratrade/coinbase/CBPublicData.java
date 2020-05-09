package io.ethanfine.neuratrade.coinbase;

import io.ethanfine.neuratrade.coinbase.models.CBProduct;
import io.ethanfine.neuratrade.coinbase.models.CBTimeGranularity;
import io.ethanfine.neuratrade.networking.NetworkingManager;
import io.ethanfine.neuratrade.networking.exceptions.NetworkRequestException;
import io.ethanfine.neuratrade.util.Constants;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import io.ethanfine.neuratrade.util.Util;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.riversun.promise.SyncPromise;
import org.ta4j.core.*;

public class CBPublicData {

    /*
    Get the epoch time (in seconds) from Coinbase Pro
     */
    public static Long getCBTime() {
        String cbAPIEndPtUrlString = Constants.CB_API_URL + Constants.CB_API_ENDPOINT_TIME;
        AtomicLong epochTime = new AtomicLong(-1);
        SyncPromise.resolve()
                .then(NetworkingManager.performGetRequest(cbAPIEndPtUrlString))
                .always((action, data) -> {
                    if (data instanceof String) {
                        String get_response = (String) data;
                        try {
                            JSONParser parser = new JSONParser();
                            JSONObject jObj = (JSONObject) parser.parse(get_response);
                            epochTime.set((long) ((double) jObj.get("epoch")));
                            action.resolve();
                        } catch (Exception e) {
                            action.reject();
                            throw new NetworkRequestException("Failed to parse response from request with URL " + cbAPIEndPtUrlString);
                        }
                    } else {
                        action.reject();
                    }
                })
                .start();
        return (epochTime.get() == -1) ? null : epochTime.get();
    }

    public static Double getTickerPrice(CBProduct product) {
        String cbAPIEndPtUrlString = Constants.CB_API_URL + Constants.CB_API_ENDPOINT_TICKER(product);
        AtomicReference<Double> price = new AtomicReference<>(-1.0);
        SyncPromise.resolve()
                .then(NetworkingManager.performGetRequest(cbAPIEndPtUrlString))
                .always((action, data) -> {
                    if (data instanceof String) {
                        String get_response = (String) data;
                        try {
                            JSONParser parser = new JSONParser();
                            JSONObject jObj = (JSONObject) parser.parse(get_response);
                            price.set(Double.parseDouble((String) jObj.get("price")));
                            action.resolve();
                        } catch (Exception e) {
                            action.reject();
                            throw new NetworkRequestException("Failed to parse response from request with URL " + cbAPIEndPtUrlString);
                        }
                    } else {
                        action.reject();
                    }
                })
                .start();
        return (price.get() == -1) ? null : price.get();
    }

    public static BarSeries getBarSeries(CBProduct product, long startTime, long endTime, CBTimeGranularity timeGranularity) {
        // Cap the number of bars to 300 due to API restrictions
        if (((endTime - startTime) / timeGranularity.seconds) > 300) {
            endTime = startTime + timeGranularity.seconds * 300;
        }

        BarSeries barSeries = new BaseBarSeriesBuilder().withName(product.productName).build();
        String cbAPIEndPtUrlString = Constants.CB_API_URL + Constants.CB_API_ENDPOINT_HISTORIC_RATES(product, startTime, endTime, timeGranularity);
        AtomicReference<String> get_response = new AtomicReference<>(null);
        SyncPromise.resolve()
                .then(NetworkingManager.performGetRequest(cbAPIEndPtUrlString))
                .always((action, data) -> {
                    if (data instanceof String) {
                        get_response.set((String) data);
                        action.resolve();
                    } else {
                        action.reject();
                    }
                })
                .start();
        if (get_response.get() == null) {
            return barSeries;
        }

        double[][] bars = Util.stringToDeep(get_response.get());
        for (double[] bar : bars) {
            ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochSecond((long) bar[0]), ZoneId.systemDefault());
            switch (timeGranularity) {
                case MINUTE:
                    zonedDateTime = zonedDateTime.plusMinutes(1);
                case MINUTE_FIVE:
                    zonedDateTime = zonedDateTime.plusMinutes(5);
                case MINUTE_FIFTEEN:
                    zonedDateTime = zonedDateTime.plusMinutes(15);
                case HOUR:
                    zonedDateTime = zonedDateTime.plusHours(1);
                case HOUR_SIX:
                    zonedDateTime = zonedDateTime.plusHours(6);
                case DAY:
                    zonedDateTime = zonedDateTime.plusDays(1);
            }

            double open = bar[3];
            double high = bar[2];
            double low = bar[1];
            double close = bar[4];
            double volume = bar[5];
            barSeries.addBar(zonedDateTime, open, high, low, close, volume);
        }

        return barSeries;
    }

    public static BarSeries getBarSeries(CBProduct product, long startTime, int barCount, CBTimeGranularity timeGranularity) {
        long endTime = startTime + barCount * timeGranularity.seconds;
        return getBarSeries(product, startTime, endTime, timeGranularity);
    }

    public static BarSeries getBarSeries(CBProduct product, int barCount, long endTime, CBTimeGranularity timeGranularity) {
        long startTime = endTime - barCount * timeGranularity.seconds;
        return getBarSeries(product, startTime, endTime, timeGranularity);
    }

    public static BarSeries getRecentBarSeries(CBProduct product, int barCount, CBTimeGranularity timeGranularity) {
        long endTime = getCBTime();
        return getBarSeries(product, barCount, endTime, timeGranularity);
    }

}
