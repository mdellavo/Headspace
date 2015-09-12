package org.quuux.headspace.net;


import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.EventLog;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;

import org.quuux.headspace.data.Station;
import org.quuux.headspace.data.StreamMetaData;
import org.quuux.headspace.events.StationUpdate;
import org.quuux.headspace.util.Log;
import org.quuux.headspace.data.Playlist;
import org.quuux.headspace.events.EventBus;
import org.quuux.headspace.events.PlayerError;
import org.quuux.headspace.events.PlayerStateChange;
import org.quuux.headspace.events.StreamMetaDataUpdate;

public class Streamer implements ExoPlayer.Listener, IcyDataSource.Listener, Playlist.Listener {

    private static final String TAG = Log.buildTag(Streamer.class);

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 160;

    private static Streamer instance = null;

    private ExoPlayer player;
    private Handler handler;

    private Station station;
    private StreamMetaData lastMetaData;

    protected Streamer() {
        handler = new Handler(Looper.getMainLooper());
        player = ExoPlayer.Factory.newInstance(1);
        player.addListener(this);
    }

    public static Streamer getInstance() {

        if (instance == null) {
            instance = new Streamer();
        }

        return instance;
    }

    private void loadStream(final String url) {

        stop();

        final Uri uri = Uri.parse(url);
        final Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
        final DataSource dataSource = new IcyDataSource(this);
        final ExtractorSampleSource sampleSource = new ExtractorSampleSource(uri, dataSource, allocator, BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);
        final MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource);
        player.prepare(audioRenderer);

        start();
    }

    private void loadStream(final Playlist playlist) {

        final String url = playlist.getTrackFile(1);
        if (url == null)
            return;

        loadStream(url);
    }

    private void destroy() {
        Log.d(TAG, "destroy");
        player.release();
    }

    public void start() {
        Log.d(TAG, "preparing...");
        player.setPlayWhenReady(true);
    }

    public void pause() {
        Log.d(TAG, "pause");
        player.setPlayWhenReady(false);
    }

    public void stop() {
        Log.d(TAG, "stop");
        player.stop();
        player.seekTo(0);
    }

    public boolean isPlaying() {
        return player.getPlayWhenReady();
    }

    @Override
    public void onPlayerStateChanged(final boolean playWhenReady, final int playbackState) {
        Log.d(TAG, "onPlayerStateChanged(playWhenReady=%s / playbackState=%s)", playWhenReady, playbackState);
        EventBus.getInstance().post(new PlayerStateChange(playWhenReady, playbackState));
    }

    @Override
    public void onPlayWhenReadyCommitted() {
        Log.d(TAG, "onPlayWhenReadyComitted");
        EventBus.getInstance().post(new PlayerStateChange(player.getPlayWhenReady(), player.getPlaybackState()));
    }

    @Override
    public void onPlayerError(final ExoPlaybackException error) {
        Log.d(TAG, "onPlayerReady(error=%s)", error, error);
        EventBus.getInstance().post(new PlayerError(error));
    }

    @Override
    public void onMetaData(final StreamMetaData metadata) {
        lastMetaData = metadata;
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onMetadata(metadata=%s)", metadata);
                EventBus.getInstance().post(new StreamMetaDataUpdate(metadata));
            }
        });
    }

    public void loadStation(final Station station) {
        this.station = station;
        if (station.hasPlaylists()) {
            final String playlistUrl = station.getPlaylists().get(0);
            Log.d(TAG, "loading playlist %s", playlistUrl);
            Playlist.parseAsyc(playlistUrl, this);
        } else if (station.hasStreams()) {
            loadStream(station.getStreams().get(0));
        }

        EventBus.getInstance().post(new StationUpdate(station));
    }

    @Override
    public void onPlaylistLoaded(final Playlist playlist) {
        if (playlist == null)
            return;

        loadStream(playlist);
    }

    public StreamMetaData getLastMetaData() {
        return lastMetaData;
    }

    public boolean isStopped() {
        return player.getPlaybackState() == ExoPlayer.STATE_IDLE;
    }

    public Station getStation() {
        return station;
    }
}
