package org.quuux.headspace.events;

public class PlayerError {
    public final Throwable error;

    public PlayerError(final Throwable error) {
        this.error = error;
    }
}
