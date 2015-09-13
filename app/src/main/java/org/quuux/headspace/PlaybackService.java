package org.quuux.headspace;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.quuux.headspace.data.Station;
import org.quuux.headspace.events.EventBus;
import org.quuux.headspace.events.PlayerError;
import org.quuux.headspace.events.PlayerStateChange;
import org.quuux.headspace.events.StationUpdate;
import org.quuux.headspace.events.StreamMetaDataUpdate;
import org.quuux.headspace.net.Streamer;
import org.quuux.headspace.ui.PlaybackNotification;
import org.quuux.headspace.util.Log;

public class PlaybackService extends Service implements AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = Log.buildTag(PlaybackService.class);

    public static final String ACTION_TOGGLE_PLAYBACK = "org.quuux.headspace.actions.TOGGLE_PLAYBACK";
    public static final String ACTION_STOP_PLAYBACK = "org.quuux.headspace.actions.STOP_PLAYBACK";
    public static final String ACTION_PAUSE_PLAYBACK = "org.quuux.headspace.actions.PAUSE_PLAYBACK";

    private static final int NOTIFICATION_ID = 1231231;
    private static final String MEDIA_SESSION_TAG = "headspace";

    private final IBinder binder = new LocalBinder();
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;

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
        } else if (ACTION_PAUSE_PLAYBACK.equals(action)) {
            setPlaying(false);
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
        setPlaying(!streamer.isPlaying());
    }

    public void setPlaying(final boolean state) {
        final Streamer streamer = Streamer.getInstance();

        if (!state) {
            streamer.pause();
        } else {
            if (streamer.isStopped())
                streamer.loadStation(streamer.getStation());

            streamer.start();
        }
    }

    private boolean requestAudioFocus() {
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private boolean releaseAudioFocus() {
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final int result = audioManager.abandonAudioFocus(this);
        return result == AudioManager.AUDIOFOCUS_GAIN;
    }

    public void stopPlayback() {
        final Streamer streamer = Streamer.getInstance();
        streamer.stop();
        releaseAudioFocus();
    }

    private void buildNotification(final Bitmap bitmap) {
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        final Notification notification = PlaybackNotification.getInstance(this, bitmap);
        nm.notify(NOTIFICATION_ID, notification);
        startForeground(NOTIFICATION_ID, notification);
    }

    private void updateNotification() {
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        final Streamer streamer = Streamer.getInstance();
        if (streamer.isStopped()) {
            nm.cancel(NOTIFICATION_ID);
            stopForeground(true);
        } else {

            Picasso.with(this).load(streamer.getStation().getIconUrl()).into(new Target() {
                @Override
                public void onBitmapLoaded(final Bitmap bitmap, final Picasso.LoadedFrom from) {
                    buildNotification(bitmap);
                }

                @Override
                public void onBitmapFailed(final Drawable errorDrawable) {
                    buildNotification(null);
                }

                @Override
                public void onPrepareLoad(final Drawable placeHolderDrawable) {
                    buildNotification(null);
                }
            });
        }
    }

    public boolean isPlaying() {
        return Streamer.getInstance().isPlaying();
    }

    @Override
    public void onAudioFocusChange(final int focusChange) {
        switch (focusChange) {

            case AudioManager.AUDIOFOCUS_GAIN:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE:
            case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK:
                setPlaying(true);
                unduck();
                break;


            case AudioManager.AUDIOFOCUS_LOSS:
                stopPlayback();
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                setPlaying(false);
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                duck();
                break;

            default:
                break;
        }
    }

    private void duck() {
        Streamer.getInstance().setVolume(.25f);
    }

    private void unduck() {
        Streamer.getInstance().setVolume(1);
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
        if (Streamer.getInstance().isPlaying()) {
            ensureLocked();
            requestAudioFocus();
        } else {
            ensureUnlocked();
            releaseAudioFocus();
        }
    }

    private void ensureLocked() {
        if (wakeLock == null) {
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "headspace");
            wakeLock.acquire();
        }

        if (wifiLock == null) {
            wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, "headspace");
            wifiLock.acquire();
        }
    }

    private void ensureUnlocked() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
        }

        if (wifiLock != null) {
            wifiLock.release();
            wifiLock = null;
        }
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
