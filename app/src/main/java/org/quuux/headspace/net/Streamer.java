package org.quuux.headspace.net;


import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.hls.HlsChunkSource;
import com.google.android.exoplayer.hls.HlsPlaylist;
import com.google.android.exoplayer.hls.HlsPlaylistParser;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.ManifestFetcher;

import org.quuux.headspace.data.Station;
import org.quuux.headspace.data.StreamMetaData;
import org.quuux.headspace.events.StationUpdate;
import org.quuux.headspace.util.Log;
import org.quuux.headspace.data.Playlist;
import org.quuux.headspace.events.EventBus;
import org.quuux.headspace.events.PlayerError;
import org.quuux.headspace.events.PlayerStateChange;
import org.quuux.headspace.events.StreamMetaDataUpdate;

import java.io.IOException;

public class Streamer implements ExoPlayer.Listener, IcyDataSource.Listener, Playlist.Listener {

    private static final String TAG = Log.buildTag(Streamer.class);

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 160;

    private static Streamer instance = null;
    private  DefaultUriDataSource uriDataSource;

    private ExoPlayer player;
    private Handler handler;

    private Station station;
    private StreamMetaData lastMetaData;
    private MediaCodecAudioTrackRenderer audioRenderer;
    private DefaultBandwidthMeter bandwidthMeter;
    private ManifestFetcher<HlsPlaylist> playlistFetcher;

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

    public void initialize(final Context context) {
        bandwidthMeter = new DefaultBandwidthMeter();
        uriDataSource = new DefaultUriDataSource(context, bandwidthMeter, "headspace/1.0");
    }

    private void loadHlsStream(final String url) {
        playlistFetcher = new ManifestFetcher<>(url, uriDataSource, new HlsPlaylistParser());
        playlistFetcher.singleLoad(Looper.getMainLooper(), new ManifestFetcher.ManifestCallback<HlsPlaylist>() {
            @Override
            public void onSingleManifest(final HlsPlaylist hlsPlaylist) {

                LoadControl loadControl = new DefaultLoadControl(new DefaultAllocator(BUFFER_SEGMENT_SIZE));
                HlsChunkSource chunkSource = new HlsChunkSource(uriDataSource, url, hlsPlaylist, bandwidthMeter,
                        null, HlsChunkSource.ADAPTIVE_MODE_SPLICE, null);
                HlsSampleSource sampleSource = new HlsSampleSource(chunkSource, loadControl,
                        BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);

                audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource);
                player.prepare(audioRenderer);
            }

            @Override
            public void onSingleManifestError(final IOException e) {

            }
        });
    }

    private void loadIcyStream(final String url) {
        final Uri uri = Uri.parse(url);
        final Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);

        final DataSource dataSource = new IcyDataSource(this);
        final ExtractorSampleSource sampleSource = new ExtractorSampleSource(uri, dataSource, allocator, BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);

        audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource);
        player.prepare(audioRenderer);
    }


    private void loadStream(final Playlist playlist) {

        final String url = playlist.getTrackFile(1);
        if (url == null)
            return;

        loadIcyStream(url);
    }

    public void destroy() {
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

    public void setVolume(final float volume){
        player.sendMessage(audioRenderer, MediaCodecAudioTrackRenderer.MSG_SET_VOLUME, volume);
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
        if (station == null)
            return;

        stop();

        this.station = station;

        Log.d(TAG, "loading station %s", station);

        if (station.hasPlaylists()) {
            final String playlistUrl = station.getPlaylists().get(0);
            Playlist.parseAsyc(playlistUrl, this);
        } else if (station.hasHlsStreams()) {
            loadHlsStream(station.getHlsStreams().get(0));
        } else if (station.hasStreams()) {
            loadIcyStream(station.getStreams().get(0));
        }

        start();

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
