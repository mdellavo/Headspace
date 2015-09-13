package org.quuux.headspace.ui;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;

import com.squareup.picasso.Picasso;

import org.quuux.headspace.MainActivity;
import org.quuux.headspace.PlaybackService;
import org.quuux.headspace.R;
import org.quuux.headspace.data.Station;
import org.quuux.headspace.data.StreamMetaData;
import org.quuux.headspace.net.Streamer;

public class PlaybackNotification {

    public static Notification getInstance(final Context context, final Bitmap bitmap) {
        final Notification.Builder builder = new Notification.Builder(context);

        final Streamer stream = Streamer.getInstance();
        final Station station = stream.getStation();
        final StreamMetaData metadata = stream.getLastMetaData();

        String text = metadata != null ? metadata.get("StreamTitle") : null;
        if (TextUtils.isEmpty(text))
            text = station.getDescription();

        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setSmallIcon(R.mipmap.ic_play);
        if (bitmap != null)
            builder.setLargeIcon(bitmap);
        builder.setContentTitle(station.getName());
        builder.setContentText(text);
        builder.setContentInfo("Headspace");

        final int playbackIcon = stream.isPlaying() ? R.mipmap.ic_pause : R.mipmap.ic_play;
        final String playbackText = context.getString(stream.isPlaying() ? R.string.action_pause : R.string.action_play);
        final Intent playbackIntent = new Intent(context, PlaybackService.class);
        playbackIntent.setAction(PlaybackService.ACTION_TOGGLE_PLAYBACK);

        final Intent stopIntent = new Intent(context, PlaybackService.class);
        stopIntent.setAction(PlaybackService.ACTION_STOP_PLAYBACK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            builder.setStyle(new Notification.MediaStyle().setShowActionsInCompactView());
            final Notification.Action play = new Notification.Action.Builder(playbackIcon, playbackText, PendingIntent.getService(context, 0, playbackIntent, 0)).build();
            builder.addAction(play);

            final Notification.Action stop = new Notification.Action.Builder(R.mipmap.ic_stop, context.getString(R.string.stop), PendingIntent.getService(context, 0, stopIntent, 0)).build();
            builder.addAction(stop);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            builder.addAction(playbackIcon, playbackText, PendingIntent.getService(context, 0, playbackIntent, 0));
            builder.addAction(R.mipmap.ic_stop, context.getString(R.string.stop), PendingIntent.getService(context, 0, stopIntent, 0));
        }

        final Intent intent = new Intent(context, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, intent, 0));

        return builder.getNotification();
    }

}
