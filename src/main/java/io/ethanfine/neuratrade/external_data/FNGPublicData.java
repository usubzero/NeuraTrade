package io.ethanfine.neuratrade.external_data;

import io.ethanfine.neuratrade.networking.NetworkingManager;
import io.ethanfine.neuratrade.networking.exceptions.NetworkRequestException;
import io.ethanfine.neuratrade.util.Constants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.riversun.promise.SyncPromise;

import java.util.*;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;

public class FNGPublicData {

    /*
    Returns a map with epoch times in seconds as keys and fear and greed index values as values
     */
    public static Map<Long, Integer> getFNGIndexDataPoints(int valueCount) {
        if (valueCount > 830) {
            valueCount = 830; // API data availability
        }

        String fgdAPIDataString = Constants.FNG_API_DATA(valueCount);
        Map<Long, Integer> dataPointMap = Collections.synchronizedMap(new TreeMap<>());
        SyncPromise.resolve()
                .then(NetworkingManager.performGetRequest(fgdAPIDataString))
                .always((action, data) -> {
                    if (data instanceof String) {
                        String get_response = (String) data;
                        try {
                            JSONParser parser = new JSONParser();
                            JSONObject jObj = (JSONObject) parser.parse(get_response);
                            JSONArray slideContent = (JSONArray) jObj.get("data");
                            Iterator iterator = slideContent.iterator();
                            while (iterator.hasNext()) {
                                JSONObject dataPoint = (JSONObject) iterator.next();
                                dataPointMap.put(Long.valueOf((String) dataPoint.get("timestamp")), Integer.valueOf((String) dataPoint.get("value")));
                            }
                            action.resolve();
                        } catch (Exception e) {
                            action.reject();
                            throw new NetworkRequestException("Failed to parse response from request with URL " + fgdAPIDataString);
                        }
                    } else {
                        action.reject();
                    }
                })
                .start();
        return dataPointMap;
    }

}
