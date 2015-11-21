package org.quuux.headspace.data;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.quuux.headspace.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Directory {

    private static final String TAG = Log.buildTag(Directory.class);
    private static List<Station> stations = null;

    private static Map<String, Station> stationIndex = new HashMap<>();

    public static void loadStations(final Context context) {
        stations = new ArrayList<>();

        final AssetManager assetManager = context.getAssets();

        String paths[] = new String[0];
        try {
            paths = assetManager.list("stations");
        } catch (IOException e) {
            Log.e(TAG, "error loading stations", e);
            return;
        }

        for (String path : paths) {
            if (!path.endsWith(".json"))
                continue;
            final Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Station>>() {}.getType();
            List<Station> chunk = null;
            try {
                chunk = gson.fromJson(new InputStreamReader(assetManager.open("stations/" + path), "UTF-8"), listType);
                stations.addAll(chunk);
            } catch (IOException e) {
                Log.e(TAG, "error loading station: %s", e, path);
            }
        }

        Station.sort(stations);

        indexStations(stations);

    }

    private static void indexStations(final List<Station> stations) {
        stationIndex.clear();

        for (Station station : stations) {
            stationIndex.put(station.getStationUrl(), station);
        }
    }

    public static List<Station> getStations() {
        return stations;
    }

    public static Station getStation(final String url) {
        return stationIndex.get(url);
    }
}
