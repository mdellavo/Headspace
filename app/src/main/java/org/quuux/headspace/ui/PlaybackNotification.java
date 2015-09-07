package org.quuux.headspace.ui;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import org.quuux.headspace.MainActivity;
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
        final Intent intent = new Intent(context, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, 0));

        return builder.build();
    }

}
