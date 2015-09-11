package org.quuux.headspace.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import org.quuux.headspace.R;
import org.quuux.headspace.data.StreamMetaData;
import org.quuux.headspace.events.PlayerStateChange;
import org.quuux.headspace.events.PlaylistUpdate;
import org.quuux.headspace.events.StreamMetaDataUpdate;
import org.quuux.headspace.util.Log;


public class PlayerView extends RelativeLayout {

    private static final String TAG = Log.buildTag(PlayerView.class);

    private TextView streamView, titleView, urlView;
    private ImageButton playbackButton;

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
        streamView = (TextView) findViewById(R.id.stream);
        titleView = (TextView) findViewById(R.id.title);
        urlView = (TextView) findViewById(R.id.url);
        playbackButton = (ImageButton) findViewById(R.id.playback);
    }

    public void updateStreamInfo(final StreamMetaData metadata) {
        titleView.setText(metadata.get("StreamTitle"));
        urlView.setText(metadata.get("StreamUrl"));
    }

    @Subscribe
    public void onMetadataUpdated(final StreamMetaDataUpdate update) {
        updateStreamInfo(update.metadata);
    }

    @Subscribe
    public void onPlayerStateChanged(final PlayerStateChange update) {
        playbackButton.setImageResource(update.playWhenReady ? R.mipmap.ic_pause : R.mipmap.ic_play);
    }

    @Subscribe
    public void onPlaylistLoaded(final PlaylistUpdate update) {
        Log.d(TAG, "onPlaylistLoaded(playlist=%s)", update.playlist);
        streamView.setText(update.playlist.getTrackTitle(update.track));
    }

    public void setOnClickListener(final OnClickListener listener) {
        playbackButton.setOnClickListener(listener);
    }
}
