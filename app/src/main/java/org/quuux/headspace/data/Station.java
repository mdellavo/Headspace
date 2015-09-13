package org.quuux.headspace.data;

import java.util.ArrayList;
import java.util.List;

public class Station {

    private String stationUrl, name, description, iconUrl, network;
    private List<String> streams = new ArrayList<>();
    private List<String> playlists = new ArrayList<>();
    private boolean commercials;


    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Station station = (Station) o;

        return stationUrl.equals(station.stationUrl);
    }

    @Override
    public int hashCode() {
        return stationUrl.hashCode();
    }

    public String getStationUrl() {
        return stationUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getStreams() {
        return streams;
    }

    public List<String> getPlaylists() {
        return playlists;
    }

    public boolean hasPlaylists() {
        return playlists.size() > 0;
    }

    public boolean hasStreams() {
        return streams.size() > 0;
    }

    public String getNetwork() {
        return network;
    }

    public boolean hasCommercials() {
        return commercials;
    }
}
