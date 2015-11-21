package org.quuux.headspace.data;

import java.util.HashMap;

public class StreamMetaData extends HashMap<String, String> {
    public String getTitle() {
        return get("StreamTitle");
    }
}
