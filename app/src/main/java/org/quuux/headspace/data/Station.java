package org.quuux.headspace.data;

import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Station implements Serializable {

    private String stationUrl, name, description, iconUrl, network;
    private List<String> streams = new ArrayList<>();
    private List<String> playlists = new ArrayList<>();
    private List<String> hlsStreams = new ArrayList<>();
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

    public boolean hasHlsStreams() {
        return hlsStreams.size() > 0;
    }

    public List<String>  getHlsStreams() {
        return hlsStreams;
    }

    public String getNetwork() {
        return network;
    }

    public boolean hasCommercials() {
        return commercials;
    }

    public boolean matchesQuery(String query) {
        query = query.toLowerCase();
        return name.toLowerCase().contains(query) ||
                description.toLowerCase().contains(query) ||
                network.toLowerCase().contains(query);
    }

    public static void sort(final List<Station> stations) {
        Collections.sort(stations, new Comparator<Station>() {
            @Override
            public int compare(final Station lhs, final Station rhs) {
                return lhs.getName().compareToIgnoreCase(rhs.getName());
            }
        });
    }
}
