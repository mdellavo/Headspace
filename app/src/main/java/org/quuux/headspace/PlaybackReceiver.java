package org.quuux.headspace;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;

public class PlaybackReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
            final Intent i = new Intent(context, PlaybackService.class);
            i.setAction(PlaybackService.ACTION_PAUSE_PLAYBACK);
            context.startService(i);
        }
    }
}
