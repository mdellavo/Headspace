package org.quuux.headspace;

import android.app.Application;

import org.quuux.headspace.data.CacheManager;
import org.quuux.headspace.data.Directory;
import org.quuux.headspace.data.Favorites;
import org.quuux.headspace.net.Streamer;
import org.quuux.headspace.util.Log;


public class HeadspaceApplication extends Application {

    private static final String TAG = Log.buildTag(HeadspaceApplication.class);

    @Override
    public void onCreate() {
        super.onCreate();

        CacheManager.initialize(this);
        Directory.loadStations(this);
        Favorites.load();

        Streamer.getInstance().initialize(this);
    }
}