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


public class MainActivity extends AppCompatActivity {

    private static final String TAG = Log.buildTag(MainActivity.class);

    private TextView title, url;
    private ImageButton playback;

    private Streamer streamer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        title = (TextView) findViewById(R.id.title);
        url = (TextView) findViewById(R.id.url);
        playback = (ImageButton) findViewById(R.id.playback);
        playback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (streamer.isPlaying())
                    streamer.pause();
                else
                    streamer.start();
            }
        });

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        streamer = Streamer.getInstance(this, "http://ice.somafm.com/indiepop", new Streamer.Listener() {
            @Override
            public void onMetaData(final Map<String, String> metadata) {
                title.setText(metadata.get("StreamTitle"));
                url.setText(metadata.get("StreamUrl"));
            }

            @Override
            public void onPlayerStateChanged(final boolean playWhenReady, final int state) {
                playback.setImageResource(playWhenReady ? R.mipmap.ic_pause : R.mipmap.ic_play);
            }

            @Override
            public void onError(final Exception error) {

            }
        });
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
}
