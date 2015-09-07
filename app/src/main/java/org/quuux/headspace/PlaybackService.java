package org.quuux.headspace;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.squareup.otto.Subscribe;

import org.quuux.headspace.data.StreamMetaData;
import org.quuux.headspace.events.EventBus;
import org.quuux.headspace.events.PlayerError;
import org.quuux.headspace.events.PlayerStateChange;
import org.quuux.headspace.events.PlaylistUpdate;
import org.quuux.headspace.events.StreamMetaDataUpdate;
import org.quuux.headspace.net.Streamer;
import org.quuux.headspace.ui.PlaybackNotification;

public class PlaybackService extends Service {
    private static final int NOTIFICATION_ID = 1231231;
    private final IBinder binder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getInstance().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getInstance().unregister(this);
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return binder;
    }

    public void loadPlaylist(final String playlistUrl) {
        Streamer.getInstance().loadPlaylist(playlistUrl);
    }

    public void playPlayback() {
        Streamer.getInstance().start();
    }

    public void pausePlayback() {
        Streamer.getInstance().pause();
    }

    public void togglePlayback() {
        final Streamer streamer = Streamer.getInstance();
        if (streamer.isPlaying())
            streamer.pause();
        else
            streamer.start();
    }

    private void updateNotification(final StreamMetaData metaData) {
        final Notification notification = PlaybackNotification.getInstance(this, metaData);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, notification);
        startForeground(NOTIFICATION_ID, notification);
    }

    private void updateNotification() {
        updateNotification(Streamer.getInstance().getLastMetaData());
    }

    public boolean isPlaying() {
        return Streamer.getInstance().isPlaying();
    }

    public class LocalBinder extends Binder {
        PlaybackService getService() {
            return PlaybackService.this;
        }
    }

    @Subscribe
    public void onMetadataUpdated(final StreamMetaDataUpdate update) {
        updateNotification(update.metadata);
    }

    @Subscribe
    public void onPlayerStateChanged(final PlayerStateChange update) {
        updateNotification();
    }

    @Subscribe
    public void onError(final PlayerError error) {
        updateNotification();
    }

    @Subscribe
    public void onPlaylistLoaded(final PlaylistUpdate update) {
        updateNotification();
    }

}
