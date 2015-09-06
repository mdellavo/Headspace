package org.quuux.headspace;

import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.quuux.headspace.events.EventBus;
import org.quuux.headspace.events.PlayerError;
import org.quuux.headspace.events.PlayerStateChange;
import org.quuux.headspace.events.StreamMetaDataUpdate;
import org.quuux.headspace.events.PlaylistUpdate;
import org.quuux.headspace.net.Streamer;
import org.quuux.headspace.util.Log;

import java.util.Map;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = Log.buildTag(MainActivity.class);

    private static final String[] playlists = new String[] {
            "http://somafm.com/7soul.pls",
            "http://somafm.com/bagel.pls",
            "http://somafm.com/beatblender.pls",
            "http://somafm.com/bootliquor.pls",
            "http://somafm.com/brfm.pls",
            "http://somafm.com/cliqhop.pls",
            "http://somafm.com/covers.pls",
            "http://somafm.com/deepspaceone.pls",
            "http://somafm.com/defcon.pls",
            "http://somafm.com/digitalis.pls",
            "http://somafm.com/doomed.pls",
            "http://somafm.com/dronezone.pls",
            "http://somafm.com/dubstep.pls",
            "http://somafm.com/earwaves.pls",
            "http://somafm.com/fluid.pls",
            "http://somafm.com/folkfwd.pls",
            "http://somafm.com/groovesalad.pls",
            "http://somafm.com/illstreet.pls",
            "http://somafm.com/indiepop.pls",
            "http://somafm.com/lush.pls",
            "http://somafm.com/metal.pls",
            "http://somafm.com/missioncontrol.pls",
            "http://somafm.com/poptron.pls",
            "http://somafm.com/secretagent.pls",
            "http://somafm.com/seventies.pls",
            "http://somafm.com/sf1033.pls",
            "http://somafm.com/sonicuniverse.pls",
            "http://somafm.com/spacestation.pls",
            "http://somafm.com/suburbsofgoa.pls",
            "http://somafm.com/thetrip.pls",
            "http://somafm.com/thistle.pls",
            "http://somafm.com/u80s.pls",
    };

    private TextView streamView, titleView, urlView;
    private ImageButton playbackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Streamer streamer = Streamer.getInstance();

        streamView = (TextView) findViewById(R.id.stream);
        titleView = (TextView) findViewById(R.id.title);
        urlView = (TextView) findViewById(R.id.url);
        playbackButton = (ImageButton) findViewById(R.id.playback);
        playbackButton.setOnClickListener(this);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        final String url = playlists[((int) (System.currentTimeMillis() % playlists.length))];
        streamer.loadPlaylist(url);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getInstance().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getInstance().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateStreamInfo(final Map<String, String> metadata) {
        titleView.setText(metadata.get("StreamTitle"));
        urlView.setText(metadata.get("StreamUrl"));
    }

    @Subscribe
    public void onMetadataUpdated(final StreamMetaDataUpdate update) {
        updateStreamInfo(update.metadata);
    }

    @Subscribe
    public void onPlayerStateChanged(final PlayerStateChange update) {
        playbackButton.setImageResource(update.playWhenReady ? R.mipmap.ic_pause : R.mipmap.ic_play);
    }

    @Subscribe
    public void onError(final PlayerError error) {
        Log.e(TAG, "player error", error.error);
        Toast.makeText(this, error.error.toString(), Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onPlaylistLoaded(final PlaylistUpdate update) {
        Log.d(TAG, "onPlaylistLoaded(playlist=%s)", update.playlist);
        streamView.setText(update.playlist.getTrackTitle(update.track));
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.playback:
                togglePlayback();
        }
    }

    private void togglePlayback() {
        final Streamer streamer = Streamer.getInstance();
        if (streamer.isPlaying())
            streamer.pause();
        else
            streamer.start();
    }
}
