package org.quuux.headspace.events;


import java.util.Map;

public class StreamMetaDataUpdate {
    public final Map<String, String> metadata;

    public StreamMetaDataUpdate(final Map<String, String> metadata) {
        this.metadata = metadata;
    }
}
