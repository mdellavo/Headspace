package org.quuux.headspace;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Transformation;

import org.quuux.feller.Log;
import org.quuux.headspace.data.Station;
import org.quuux.headspace.net.Streamer;
import org.quuux.headspace.ui.PaletteCache;
import org.quuux.headspace.ui.Picasso;

public class StationActivity extends AppCompatActivity {

    private static final String TAG = Log.buildTag(StationActivity.class);

    private static final String EXTRA_STATION = "station";

    private TextView stationView, networkView, nowPlayingView, descriptionView, urlView;
    private ImageView iconView;
    private View containerView;

    public static Intent getInstance(final Context context, final Station station) {
        final Intent intent = new Intent(context, StationActivity.class);
        intent.putExtra(EXTRA_STATION, station);
        return intent;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_station);

        containerView = findViewById(R.id.container);
        stationView = (TextView) findViewById(R.id.station);
        networkView = (TextView) findViewById(R.id.network);
        nowPlayingView = (TextView) findViewById(R.id.now_playing);
        descriptionView = (TextView) findViewById(R.id.description);
        urlView = (TextView) findViewById(R.id.url);
        iconView = (ImageView)findViewById(R.id.icon);

        update();
    }

    private void update() {
        update(getStation());
    }

    private Station getStation() {
        return (Station) getIntent().getSerializableExtra(EXTRA_STATION);
    }

    private void update(final Station station) {
        Log.d(TAG, "station=%s", station);
        stationView.setText(station.getName());
        networkView.setText(station.getNetwork());
        descriptionView.setText(station.getDescription());
        urlView.setText(station.getStationUrl());
        Picasso.with(this).load(station.getIconUrl()).transform(new Transformation() {
            @Override
            public Bitmap transform(final Bitmap source) {
                PaletteCache.generate(station.getIconUrl(), source);
                return source;
            }

            @Override
            public String key() {
                return station.getIconUrl();
            }
        }).fit().centerInside().into(iconView, new Callback() {
            @Override
            public void onSuccess() {
                final Palette palette = PaletteCache.get(station.getIconUrl());
                if (palette == null)
                    return;

                final Palette.Swatch swatch = palette.getVibrantSwatch();
                if (swatch != null) {
                    containerView.setBackgroundColor(swatch.getRgb());
                    stationView.setTextColor(swatch.getTitleTextColor());
                    descriptionView.setTextColor(swatch.getBodyTextColor());
                }

            }

            @Override
            public void onError() {

            }
        });

        if (station.equals(Streamer.getInstance().getStation()))
            nowPlayingView.setText(Streamer.getInstance().getLastMetaData().getTitle());
    }
}
