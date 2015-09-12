package org.quuux.headspace.events;

import org.quuux.headspace.data.Station;

public class StationUpdate {
    public final Station station;

    public StationUpdate(final Station station) {
        this.station = station;
    }
}
