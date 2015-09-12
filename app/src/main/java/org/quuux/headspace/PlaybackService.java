package org.quuux.headspace;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.squareup.otto.Subscribe;

import org.quuux.headspace.data.Station;
import org.quuux.headspace.events.EventBus;
import org.quuux.headspace.events.PlayerError;
import org.quuux.headspace.events.PlayerStateChange;
import org.quuux.headspace.events.StationUpdate;
import org.quuux.headspace.events.StreamMetaDataUpdate;
import org.quuux.headspace.net.Streamer;
import org.quuux.headspace.ui.PlaybackNotification;
import org.quuux.headspace.util.Log;

public class PlaybackService extends Service {

    private static final String TAG = Log.buildTag(PlaybackService.class);

    public static final String ACTION_TOGGLE_PLAYBACK = "org.quuux.headspace.actions.TOGGLE_PLAYBACK";
    public static final String ACTION_STOP_PLAYBACK = "org.quuux.headspace.actions.STOP_PLAYBACK";

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

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        final String action = intent.getAction();
        if (ACTION_TOGGLE_PLAYBACK.equals(action)) {
            togglePlayback();
        } else if (ACTION_STOP_PLAYBACK.equals(action)) {
            stopPlayback();
            stopSelf();
        } else {
            return super.onStartCommand(intent, flags, startId);
        }

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return binder;
    }

    public void loadStation(final Station station) {
        Streamer.getInstance().loadStation(station);
    }

    public void togglePlayback() {
        final Streamer streamer = Streamer.getInstance();

        if (streamer.isStopped()) {

        } else if (streamer.isPlaying()) {
            streamer.pause();
        } else {
            streamer.start();
        }
    }

    public void stopPlayback() {
        final Streamer streamer = Streamer.getInstance();
        streamer.stop();
    }

    private void updateNotification() {
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        final Streamer streamer = Streamer.getInstance();
        if (streamer.isStopped()) {
            nm.cancel(NOTIFICATION_ID);
            stopForeground(true);
        } else {
            final Notification notification = PlaybackNotification.getInstance(this);
            nm.notify(NOTIFICATION_ID, notification);
            startForeground(NOTIFICATION_ID, notification);
        }
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
        updateNotification();
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
    public void onStationUpdate(final StationUpdate update) {
        updateNotification();
    }

}
