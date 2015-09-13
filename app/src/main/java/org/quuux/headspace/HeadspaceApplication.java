package org.quuux.headspace;

import android.app.Application;

import org.quuux.headspace.data.Directory;
import org.quuux.headspace.data.Station;
import org.quuux.headspace.ui.Picasso;
import org.quuux.headspace.util.Log;

import java.util.List;


public class HeadspaceApplication extends Application {

    private static final String TAG = Log.buildTag(HeadspaceApplication.class);

    @Override
    public void onCreate() {
        super.onCreate();
        final List<Station> stations = Directory.getStations(this); // trigger loading
        for (Station station : stations)
            Picasso.with(this).load(station.getIconUrl()).fetch();
    }
}