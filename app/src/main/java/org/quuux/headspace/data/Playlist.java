package org.quuux.headspace.data;

import android.os.AsyncTask;

import org.ini4j.Ini;
import org.ini4j.Profile;
import org.quuux.feller.Log;

import java.io.IOException;
import java.net.URL;

public class Playlist {

    public interface Listener {
        void onPlaylistLoaded(Playlist playlist);
    }

    private static final String VERSION = "2";
    private static final String TAG = Log.buildTag(Playlist.class);

    private final Profile.Section playlist;

    protected Playlist(final Profile.Section playlist) {
        this.playlist = playlist;
    }

    public int getNumberOfEntries() {
        final String s = playlist.get("numberofentries");
        if (s == null)
            return -1;
        return Integer.valueOf(s);
    }

    public String getTrack(final String type, final int n) {
        return playlist.get(type + String.valueOf(n));
    }

    public String getTrackFile(final int n) {
        return getTrack("File", n);
    }

    public String getTrackTitle(final int n) {
        return getTrack("Title", n);
    }

    public int getTracLength(final int n) {
        return Integer.valueOf(getTrack("Length", n));
    }

    public static Playlist parse(final String url) {
        final Ini playlist;
        try {
            playlist = new Ini(new URL(url));
        } catch (IOException e) {
            Log.e(TAG, "error loading playlist", e);
            return null;
        }

        final Profile.Section section = playlist.get("playlist");
        if (section == null) {
            Log.d(TAG, "could not find playlist section");
            return null;
        }

        if (!VERSION.equals(section.get("Version"))) {
            Log.d(TAG, "version %s not supported", section.get("Version"));
            return null;
        }

        return new Playlist(section);
    }

    public static void parseAsyc(final String url, final Listener listener) {
        new PlaylistLoadTask(listener).execute(url);
    }

    static class PlaylistLoadTask extends AsyncTask<String, Void, Playlist> {

        private final Listener listener;

        PlaylistLoadTask(Listener listener) {
            this.listener = listener;
        }

        @Override
        protected Playlist doInBackground(final String... params) {
            return parse(params[0]);
        }

        @Override
        protected void onPostExecute(final Playlist playlist) {
            super.onPostExecute(playlist);
            if (listener != null)
                listener.onPlaylistLoaded(playlist);
        }
    }

}
