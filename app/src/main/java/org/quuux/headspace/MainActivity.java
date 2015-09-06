package org.quuux.headspace;

import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Map;


public class MainActivity extends AppCompatActivity implements Playlist.Listener {

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

    private TextView titleView, urlView;
    private ImageButton playbackButton;

    private Streamer streamer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titleView = (TextView) findViewById(R.id.title);
        urlView = (TextView) findViewById(R.id.url);
        playbackButton = (ImageButton) findViewById(R.id.playback);
        playbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (streamer == null)
                    return;

                if (streamer.isPlaying())
                    streamer.pause();
                else
                    streamer.start();
            }
        });

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        final String url = playlists[((int) (System.currentTimeMillis() % playlists.length))];
        loadPlaylist(url);
    }

    private void loadPlaylist(final String playlistUrl) {
        Log.d(TAG, "loading playlist %s", playlistUrl);
        Playlist.parseAsyc(playlistUrl, this);
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

    @Override
    public void onPlaylistLoaded(final Playlist playlist) {
        Log.d(TAG, "onPlaylistLoaded(playlist=%s)", playlist);

        if (playlist == null)
            return;

        final String streamUrl = playlist.getTrackFile(1);
        if (streamUrl == null)
            return;

        streamer = Streamer.getInstance(this, streamUrl, new Streamer.Listener() {
            @Override
            public void onMetaData(final Map<String, String> metadata) {
                titleView.setText(metadata.get("StreamTitle"));
                urlView.setText(metadata.get("StreamUrl"));
            }

            @Override
            public void onPlayerStateChanged(final boolean playWhenReady, final int state) {
                playbackButton.setImageResource(playWhenReady ? R.mipmap.ic_pause : R.mipmap.ic_play);
            }

            @Override
            public void onError(final Exception error) {

            }
        });
    }
}
