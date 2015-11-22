package org.quuux.headspace.data;

import org.quuux.sack.Sack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Favorites {

    private static List<String> favoriteUrls = null;

    private static File getFavoritesPath() {
        return new File(CacheManager.getRoot(), "favorites");
    }

    public static void load() {
        Sack.open(String[].class, getFavoritesPath()).load(new Sack.Listener<String[]>() {
            @Override
            public void onResult(final Sack.Status status, final String[] favorites) {
                favoriteUrls = new ArrayList<String>();
                if (favorites == null)
                    return;
                for (String favorite : favorites)
                    favoriteUrls.add(favorite);
            }
        });
    }

    public static List<Station> getFavorites() {
        final List<Station> favorites = new ArrayList<>();
        for (String url : favoriteUrls)
            favorites.add(Directory.getStation(url));

        Station.sort(favorites);
        return favorites;
    }

    public static void addFavorite(final String favoriteUrl) {
        if (!favoriteUrls.contains(favoriteUrl)) {
            favoriteUrls.add(favoriteUrl);
            commit();
        }
    }

    public static void removeFavorite(final String favoriteUrl) {
        if (favoriteUrls.contains(favoriteUrl)) {
            favoriteUrls.remove(favoriteUrl);
            commit();
        }
    }

    private static void commit() {
        final String[] urls = favoriteUrls.toArray(new String[favoriteUrls.size()]);
        Sack.open(String[].class, getFavoritesPath()).commit(urls);
    }
}
