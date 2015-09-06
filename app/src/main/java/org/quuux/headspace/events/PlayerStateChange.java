package org.quuux.headspace.events;

public class PlayerStateChange {

    public final boolean playWhenReady;
    public final int playbackState;

    public PlayerStateChange(final boolean playWhenReady, final int playbackState) {
        this.playWhenReady = playWhenReady;
        this.playbackState = playbackState;
    }
}
