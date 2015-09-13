package org.quuux.headspace;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.RemoteControlClient;
import android.media.session.MediaSession;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Target;

import org.quuux.headspace.data.Station;
import org.quuux.headspace.events.EventBus;
import org.quuux.headspace.events.PlayerError;
import org.quuux.headspace.events.PlayerStateChange;
import org.quuux.headspace.events.StationUpdate;
import org.quuux.headspace.events.StreamMetaDataUpdate;
import org.quuux.headspace.net.Streamer;
import org.quuux.headspace.ui.Picasso;
import org.quuux.headspace.ui.PlaybackNotification;
import org.quuux.headspace.util.Log;

public class PlaybackService extends Service implements AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = Log.buildTag(PlaybackService.class);

    public static final String ACTION_TOGGLE_PLAYBACK = "org.quuux.headspace.actions.TOGGLE_PLAYBACK";
    public static final String ACTION_STOP_PLAYBACK = "org.quuux.headspace.actions.STOP_PLAYBACK";
    public static final String ACTION_PAUSE_PLAYBACK = "org.quuux.headspace.actions.PAUSE_PLAYBACK";

    private static final int NOTIFICATION_ID = 1231231;

    private final IBinder binder = new LocalBinder();
    private PowerManager.WakeLock wakeLock;
    private WifiManager.WifiLock wifiLock;
    private RemoteControlClient remoteControlClient;
    private ComponentName remoteControlReceiver;
    private MediaSession mediaSession;

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getInstance().register(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaSession = new MediaSession(this, "headspace");
            mediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS | MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

            final Intent intent = new Intent(this, MainActivity.class);
            mediaSession.setSessionActivity(PendingIntent.getActivity(this, 0, intent, 0));
        } else {
            remoteControlReceiver = new ComponentName(getPackageName(), PlaybackReceiver.class.getName());

            final Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            mediaButtonIntent.setComponent(remoteControlReceiver);
            final PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0);

            remoteControlClient = new RemoteControlClient(mediaPendingIntent);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getInstance().unregister(this);
        Streamer.getInstance().destroy();
        ensureUnlocked();
        releaseAudioFocus();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaSession.release();
        }
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        final String action = intent != null ? intent.getAction() : null;
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

    private boolean requestAudioFocus() {
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final int result = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private boolean releaseAudioFocus() {
        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final int result = audioManager.abandonAudioFocus(this);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
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

    private void buildNotification(final Bitmap bitmap) {
        final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        final Notification notification = PlaybackNotification.getInstance(this, bitmap, mediaSession);
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
                public void onBitmapLoaded(final Bitmap bitmap, final com.squareup.picasso.Picasso.LoadedFrom from) {
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startMediaSession() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return;

        mediaSession.setActive(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void stopMediaSession() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return;

        mediaSession.setActive(false);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private MediaSession.Token getMediaSessionToken() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return null;

        return mediaSession != null ? mediaSession.getSessionToken() : null;
    }

    // FIXME support new api
    private void registerRemote() {
        final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (remoteControlReceiver != null) {
            am.registerMediaButtonEventReceiver(remoteControlReceiver);
        }
        if (remoteControlClient != null) {
            am.registerRemoteControlClient(remoteControlClient);
        }
    }

    private void unregisterRemote() {
        final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        if (remoteControlReceiver != null) {
            am.unregisterMediaButtonEventReceiver(remoteControlReceiver);
        }

        if (remoteControlClient != null) {
            am.unregisterRemoteControlClient(remoteControlClient);
        }
    }

    private void duck() {
        Streamer.getInstance().setVolume(.25f);
    }

    private void unduck() {
        Streamer.getInstance().setVolume(1);
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

    public void stopPlayback() {
        final Streamer streamer = Streamer.getInstance();
        streamer.stop();
        releaseAudioFocus();
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

    @Subscribe
    public void onMetadataUpdated(final StreamMetaDataUpdate update) {
        updateNotification();
    }

    @Subscribe
    public void onPlayerStateChanged(final PlayerStateChange update) {
        updateNotification();
        if (!Streamer.getInstance().isStopped()) {
            ensureLocked();
            requestAudioFocus();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startMediaSession();
            } else {
                registerRemote();
            }
        } else {
            ensureUnlocked();
            releaseAudioFocus();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                stopMediaSession();
            } else {
                unregisterRemote();
            }
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

    public class LocalBinder extends Binder {
        PlaybackService getService() {
            return PlaybackService.this;
        }
    }
}
