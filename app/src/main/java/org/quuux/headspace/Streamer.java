package org.quuux.headspace;


import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;

import java.util.Map;

public class Streamer implements ExoPlayer.Listener, IcyDataSource.Listener {

    private final Listener listener;

    public interface Listener {
        void onMetaData(Map<String, String> metadata);
        void onPlayerStateChanged(boolean playWhenReady, int state);
        void onError(Exception error);
    }

    private static final String TAG = Log.buildTag(Streamer.class);

    private static final String USER_AGENT = "headspace-app/0.1";

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 160;

    private static Streamer instance = null;

    private ExoPlayer player;
    private Handler handler;

    protected Streamer(final Listener listener) {
        handler = new Handler(Looper.getMainLooper());
        player = ExoPlayer.Factory.newInstance(1);
        player.addListener(this);
        this.listener = listener;
    }

    public static Streamer getInstance() {
        return instance;
    }

    public static Streamer getInstance(final Context context, final String url, final Listener listener) {

        if (instance != null) {
            instance.stop();
            instance.destory();
            instance = null;
        }

        instance = new Streamer(listener);

        final Uri uri = Uri.parse(url);
        final Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
        final DataSource dataSource = new IcyDataSource(instance);
        final ExtractorSampleSource sampleSource = new ExtractorSampleSource(
                uri, dataSource, allocator, BUFFER_SEGMENT_COUNT * BUFFER_SEGMENT_SIZE);
        final MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource);
        instance.player.prepare(audioRenderer);

        instance.start();

        return instance;
    }

    private void destory() {
        Log.d(TAG, "destrory");
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
    }

    public boolean isPlaying() {
        return player.getPlayWhenReady();
    }

    @Override
    public void onPlayerStateChanged(final boolean playWhenReady, final int playbackState) {
        Log.d(TAG, "onPlayerStateChanged(playWhenReady=%s / playbackState=%s)", playWhenReady, playbackState);
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onPlayerStateChanged(playWhenReady, playbackState);
                }
            });
        }
    }

    @Override
    public void onPlayWhenReadyCommitted() {
        Log.d(TAG, "onPlayWhenReadyComitted");
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onPlayerStateChanged(player.getPlayWhenReady(), player.getPlaybackState());
                }
            });
        }
    }

    @Override
    public void onPlayerError(final ExoPlaybackException error) {
        Log.d(TAG, "onPlayerReady(error=%s)", error);
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onError(error);
                }
            });
        }
    }

    @Override
    public void onMetaData(final Map<String, String> metadata) {
        Log.d(TAG, "onMetadata(metadata=%s)", metadata);
        if (listener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onMetaData(metadata);
                }
            });
        }
    }
}
