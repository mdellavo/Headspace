package org.quuux.headspace.events;


import org.quuux.headspace.data.StreamMetaData;

public class StreamMetaDataUpdate {
    public final StreamMetaData metadata;

    public StreamMetaDataUpdate(final StreamMetaData metadata) {
        this.metadata = metadata;
    }
}
