package org.quuux.headspace.events;

import org.quuux.headspace.data.Playlist;

public class PlaylistUpdate {
    public final Playlist playlist;
    public final int track;

    public PlaylistUpdate(final Playlist playlist, final int track) {
        this.playlist = playlist;
        this.track = track;
    }
}
