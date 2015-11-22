package org.quuux.headspace.ui;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import java.util.HashMap;
import java.util.Map;

public class PaletteCache {
    private static Map<String, Palette> paletteCache = new HashMap<>();

    public static void generate(final String key, final Bitmap bitmap) {
        if (!paletteCache.containsKey(key)) {
            final Palette palette = Palette.from(bitmap).generate();
            paletteCache.put(key, palette);
        }
    }

    public static Palette get(final String key) {
        return paletteCache.get(key);
    }
}
