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
import java.util.List;

public class Directory {

    private static final String TAG = Log.buildTag(Directory.class);
    private static List<Station> stations = null;

    private static List<Station> loadStations(final Context context) throws IOException {
        final List<Station> stations = new ArrayList<>();

        final AssetManager assetManager = context.getAssets();

        String paths[] = assetManager.list("stations");
        for (String path : paths) {
            if (!path.endsWith(".json"))
                continue;
            final Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<Station>>() {}.getType();
            List<Station> chunk = gson.fromJson(new InputStreamReader(assetManager.open("stations/" + path), "UTF-8"), listType);
            stations.addAll(chunk);
        }

        return stations;
    }

    public static List<Station> getStations(final Context context) {

        if (stations == null) {
            try {
                stations = loadStations(context);
            } catch (IOException e) {
                Log.e(TAG, "error loading stations", e);
            }
        }

        return stations;
    }


}
