package org.quuux.headspace.ui;

import android.content.Context;

import com.squareup.picasso.OkHttpDownloader;

public class Picasso {

    private static boolean initialized;

    public static com.squareup.picasso.Picasso with(final Context context) {

        if (!initialized) {
            initialized = true;
            final com.squareup.picasso.Picasso.Builder builder = new com.squareup.picasso.Picasso.Builder(context.getApplicationContext());
            //builder.loggingEnabled(BuildConfig.DEBUG);
            //builder.indicatorsEnabled(BuildConfig.DEBUG);
            builder.downloader(new OkHttpDownloader(context.getApplicationContext(), Integer.MAX_VALUE));
            com.squareup.picasso.Picasso.setSingletonInstance(builder.build());
        }

        return com.squareup.picasso.Picasso.with(context);
    }
}
