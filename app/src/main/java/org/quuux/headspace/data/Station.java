package org.quuux.headspace.data;

import java.util.ArrayList;
import java.util.List;

public class Station {

    private String stationUrl, name, description, iconUrl;
    private List<String> streams = new ArrayList<>();

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
}
