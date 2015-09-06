package org.quuux.headspace.util;


import org.quuux.headspace.BuildConfig;

public class Log  {

    private final String mTag;

    private static String sPrefix;

    public static void setsPrefix(final String prefix) {
        sPrefix = prefix;
    }

    public static String buildTag(final String tag) {
        return sPrefix == null ? tag : sPrefix + ":" + tag;
    }

    public static String buildTag(final Class klass) {
        return buildTag(klass.getName());
    }

    public static void d(final String tag, final String message, final Object...args) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(buildTag(tag), String.format(message, args));
        }
    }

    public static void d(final String tag, final String message, final Throwable tr,  Object...args) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(buildTag(tag), String.format(message, args), tr);
        }
    }

    public static void v(final String tag, final String message, final Object...args) {
        if (BuildConfig.DEBUG) {
            android.util.Log.v(buildTag(tag), String.format(message, args));
        }
    }

    public static void v(final String tag, final String message, final Throwable tr,  Object...args) {
        if (BuildConfig.DEBUG) {
            android.util.Log.v(buildTag(tag), String.format(message, args), tr);
        }
    }

    public static void i(final String tag, final String message, final Object...args) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(buildTag(tag), String.format(message, args));
        }
    }

    public static void i(final String tag, final String message, final Throwable tr,  Object...args) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(buildTag(tag), String.format(message, args), tr);
        }
    }

    public static void e(final String tag, final String message, final Object...args) {
        android.util.Log.e(buildTag(tag), String.format(message, args));
    }

    public static void e(final String tag, final String message, final Throwable tr,  Object...args) {
        android.util.Log.e(buildTag(tag), String.format(message, args), tr);
    }


    public static void w(final String tag, final String message, final Object...args) {
        android.util.Log.w(buildTag(tag), String.format(message, args));
    }

    public static void w(final String tag, final String message, final Throwable tr,  Object...args) {
        android.util.Log.e(buildTag(tag), String.format(message, args), tr);
    }

    public Log(final String tag) {
        mTag = tag;
    }

    public Log(final Class klass) {
        this(klass.getName());
    }

    public void d(final String message, final Object...args) {
        Log.d(mTag, message, args);
    }

    public void d(final String message, final Throwable tr,  Object...args) {
        Log.d(mTag, message, tr, args);
    }

    public void v(final String message, final Object...args) {
        Log.v(mTag, message, args);
    }

    public void v(final String message, final Throwable tr,  Object...args) {
        Log.v(mTag, message, tr, args);
    }

    public void i(final String message, final Object...args) {
        Log.i(mTag, message, args);
    }

    public void i(final String message, final Throwable tr,  Object...args) {
        Log.i(mTag, message, tr, args);
    }

    public void e(final String message, final Object...args) {
        Log.e(mTag, message, args);
    }

    public void e(final String message, final Throwable tr,  Object...args) {
        Log.e(mTag, message, tr, args);
    }

    public void w(final String message, final Object...args) {
        Log.w(mTag, message, args);
    }

    public void w(final String message, final Throwable tr,  Object...args) {
        Log.d(mTag, message, tr, args);
    }
}
