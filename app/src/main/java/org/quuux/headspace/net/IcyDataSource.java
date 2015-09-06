package org.quuux.headspace.net;

import com.google.android.exoplayer.C;
import com.google.android.exoplayer.upstream.DataSpec;
import com.google.android.exoplayer.upstream.HttpDataSource;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.quuux.headspace.Log;
import org.quuux.headspace.events.EventBus;
import org.quuux.headspace.events.StreamMetaDataUpdate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import okio.BufferedSource;


public class IcyDataSource implements HttpDataSource {

    public interface Listener {
        void onMetaData(Map<String, String> metadata);
    }

    private static final String TAG = Log.buildTag(IcyDataSource.class);
    private final Request.Builder builder;

    private final Listener listener;
    private OkHttpClient client;
    private int interval;
    private int remaining;
    private BufferedSource contents;
    private Response response;
    private DataSpec dataSpec;

    public IcyDataSource(final Listener listener) {
        this.listener = listener;
        client = new OkHttpClient();
        builder =  new Request.Builder().addHeader("Icy-Metadata", "1");
    }

    @Override
    public long open(final DataSpec dataSpec) throws HttpDataSourceException {
        this.dataSpec = dataSpec;

        final Request request = builder
                .url(dataSpec.uri.toString())
                .build();

        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new HttpDataSourceException(e, dataSpec);
        }

        if (!response.isSuccessful())
            throw new HttpDataSource.InvalidResponseCodeException(response.code(), response.headers().toMultimap(), dataSpec);

        // FIXME check content type
        // FIXME handle redirect?

        String metaintVal = response.headers().get("icy-metaint");
        interval = metaintVal != null ? Integer.parseInt(metaintVal) : 0;

        Log.d(TAG, "metadata interval=%s", interval);

        remaining = interval;

        try {
            contents = response.body().source();
        } catch (IOException e) {
            throw new HttpDataSourceException(e, dataSpec);
        }

        return C.LENGTH_UNBOUNDED;
    }

    @Override
    public void close() throws HttpDataSourceException {
        try {
            contents.close();
        } catch (IOException e) {
            throw new HttpDataSourceException(e, dataSpec);
        }
    }

    @Override
    public int read(final byte[] buffer, final int offset, final int readLength)throws HttpDataSourceException {
        try {
            int rv = contents.read(buffer, offset, interval > 0 && remaining < readLength ? remaining : readLength );

            if (interval > 0 && remaining == rv) {
                remaining = interval;
                readMetaData();
            } else {
                remaining -= rv;
            }

            return rv;
        } catch (IOException e) {
            throw new HttpDataSourceException(e, dataSpec);
        }
    }

    private void readMetaData() throws IOException {
        int length = contents.readByte() * 16;
        if (length > 0) {
            Log.d(TAG, "metadata length=%s", length);
            final byte[] buffer = contents.readByteArray(length);
            final Map<String, String> metadata = parseMetadata(new String(buffer));

            if (listener != null)
                listener.onMetaData(metadata);
        }
    }

    private Map<String, String> parseMetadata(final String s) {
        Log.d(TAG, "metadata=%s", s);

        final String[] parts = s.split(";");
        final Map<String, String> metadata = new HashMap<>(parts.length);

        for (String part : parts) {
            final int index = part.indexOf('=');
            if (index < 0)
                continue;
            final String key = part.substring(0, index);
            final String value = part.substring(index + 2, part.length() - 1);
            Log.d(TAG, "key=%s / value=%s", key, value);
            metadata.put(key, value);
        }

        return metadata;
    }

    @Override
    public void setRequestProperty(final String name, final String value) {
        builder.addHeader(name, value);
    }

    @Override
    public void clearRequestProperty(final String name) {
        builder.removeHeader(name);
    }

    @Override
    public void clearAllRequestProperties() {
        builder.headers(new Headers.Builder().build());
    }

    @Override
    public Map<String, List<String>> getResponseHeaders() {
        return response.headers().toMultimap();
    }

    @Override
    public String getUri() {
        return dataSpec.uri.toString();
    }
}