package org.quuux.headspace.ui;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import org.quuux.headspace.MainActivity;
import org.quuux.headspace.PlaybackService;
import org.quuux.headspace.R;
import org.quuux.headspace.data.Playlist;
import org.quuux.headspace.data.StreamMetaData;
import org.quuux.headspace.net.Streamer;

public class PlaybackNotification {

    public static Notification getInstance(final Context context, final StreamMetaData metaData) {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        final Streamer stream = Streamer.getInstance();
        final Playlist playlist = stream.getPlaylist();
        final int track = stream.getTrack();

        final String streamTitle = metaData != null ? metaData.get("StreamTitle") : null;
        final String streamUrl = metaData != null ? metaData.get("StreamUrl") : null;

        String title = playlist.getTrackTitle(track);
        if (TextUtils.isEmpty(title))
            title = streamUrl;
        if (TextUtils.isEmpty(title))
            title = playlist.getTrackFile(1);

        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setSmallIcon(R.mipmap.ic_play);
        builder.setContentTitle(title);
        builder.setContentText(streamTitle);
        builder.setContentInfo("Headspace");

        final int playbackIcon = stream.isPlaying() ? R.mipmap.ic_pause : R.mipmap.ic_play;
        final String playbackText = context.getString(stream.isPlaying() ? R.string.action_pause : R.string.action_play);
        final Intent playbackIntent = new Intent(context, PlaybackService.class);
        playbackIntent.setAction(PlaybackService.ACTION_TOGGLE_PLAYBACK);
        builder.addAction(playbackIcon, playbackText, PendingIntent.getService(context, 0, playbackIntent, 0));

        final Intent stopIntent = new Intent(context, PlaybackService.class);
        stopIntent.setAction(PlaybackService.ACTION_STOP_PLAYBACK);
        builder.addAction(R.mipmap.ic_stop, context.getString(R.string.stop), PendingIntent.getService(context, 0, stopIntent, 0));

        final Intent intent = new Intent(context, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, 0));

        return builder.build();
    }

}
