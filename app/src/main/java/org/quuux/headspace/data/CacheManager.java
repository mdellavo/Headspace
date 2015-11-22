package org.quuux.headspace.data;

import android.content.Context;

import java.io.File;

public class CacheManager {

    private static File root;

    public static void initialize(final Context context) {
        root = context.getExternalCacheDir();
    }

    public static File getRoot() {
        return root;
    }
}
