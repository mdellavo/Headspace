package org.quuux.headspace.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.quuux.feller.Log;
import org.quuux.headspace.R;
import org.quuux.headspace.data.Station;
import org.quuux.headspace.data.StreamMetaData;
import org.quuux.headspace.events.PlayerStateChange;
import org.quuux.headspace.events.StationUpdate;
import org.quuux.headspace.events.StreamMetaDataUpdate;
import org.quuux.headspace.net.Streamer;


public class PlayerView extends RelativeLayout implements View.OnClickListener {

    public interface Listener {
        void togglePlayback();
        void openStation(Station station);
    }

    private static final String TAG = Log.buildTag(PlayerView.class);

    private TextView stationView, titleView;
    private ImageButton playbackButton;
    private ImageView iconView;
    private Listener listener;

    public PlayerView(final Context context) {
        super(context);
        init();
    }

    public PlayerView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PlayerView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlayerView(final Context context, final AttributeSet attrs, final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.player_view, this);

        findViewById(R.id.container).setOnClickListener(this);

        iconView = (ImageView) findViewById(R.id.icon);
        stationView = (TextView) findViewById(R.id.station);
        titleView = (TextView) findViewById(R.id.title);

        playbackButton = (ImageButton) findViewById(R.id.playback);
        playbackButton.setOnClickListener(this);

        update();
    }

    public void setListener(final Listener listener) {
        this.listener = listener;
    }

    public void update() {
        final Streamer streamer = Streamer.getInstance();
        update(streamer.getStation(), streamer.getLastMetaData());
    }

    public void update(final Station station, final StreamMetaData metadata) {
        final boolean hasStation = station != null;
        final boolean hasMetadata = hasStation && metadata != null;

        final String text = hasStation ? String.format("%s: %s", station.getNetwork(), station.getName()) : null;
        stationView.setText(text);
        stationView.setVisibility(hasStation ? View.VISIBLE : View.GONE);

        if (hasStation)
            Picasso.with(iconView.getContext()).load(station.getIconUrl()).fit().centerCrop().into(iconView);

        titleView.setText(hasMetadata ? metadata.get("StreamTitle") : null);
        titleView.setVisibility(hasMetadata ? View.VISIBLE : View.GONE);

        final Streamer streamer = Streamer.getInstance();
        playbackButton.setImageResource(streamer.isPlaying() ? R.mipmap.ic_player_pause : R.mipmap.ic_player_play);
    }

    @Subscribe
    public void onMetadataUpdated(final StreamMetaDataUpdate update) {
        update(Streamer.getInstance().getStation(), update.metadata);
    }

    @Subscribe
    public void onPlayerStateChanged(final PlayerStateChange update) {
        update();
    }

    @Subscribe
    public void onStationChanged(final StationUpdate update) {
        Log.d(TAG, "onPlaylistLoaded(playlist=%s)", update.station);
        update(update.station, null);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.container:
                if (listener != null)
                    listener.openStation(Streamer.getInstance().getStation());
                break;

            case R.id.playback:
                if (listener != null)
                    listener.togglePlayback();
                break;

            default:
                break;
        }
    }
}
