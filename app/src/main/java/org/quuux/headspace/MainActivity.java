package org.quuux.headspace;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.quuux.headspace.data.Station;
import org.quuux.headspace.events.EventBus;
import org.quuux.headspace.events.PlayerError;
import org.quuux.headspace.events.PlayerStateChange;
import org.quuux.headspace.net.Streamer;
import org.quuux.headspace.ui.DirectoryAdapter;
import org.quuux.headspace.ui.PlayerView;
import org.quuux.headspace.util.Log;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private static final String TAG = Log.buildTag(MainActivity.class);

    private PlaybackService playbackService = null;
    private ListView directory;
    private PlayerView playerView;
    private DirectoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(new Intent(this, PlaybackService.class));

        setContentView(R.layout.activity_main);
        directory = (ListView)findViewById(R.id.directory);
        directory.setOnItemClickListener(this);

        adapter = new DirectoryAdapter(this);
        directory.setAdapter(adapter);

        playerView = (PlayerView)findViewById(R.id.player);
        playerView.setOnClickListener(this);
        onPlayerStateChanged(null);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(new Intent(this, PlaybackService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        final EventBus instance = EventBus.getInstance();
        instance.register(this);
        instance.register(playerView);

    }

    @Override
    protected void onPause() {
        super.onPause();
        final EventBus instance = EventBus.getInstance();
        instance.unregister(this);
        instance.unregister(playerView);
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

    @Subscribe
    public void onError(final PlayerError error) {
        Log.e(TAG, "player error", error.error);
        Toast.makeText(this, error.error.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.playback:
                togglePlayback();
                break;
        }
    }

    @Subscribe
    public void onPlayerStateChanged(final PlayerStateChange update) {
        playerView.setVisibility(Streamer.getInstance().isStopped() ? View.GONE : View.VISIBLE);
    }

    private void togglePlayback() {
        if (playbackService != null)
            playbackService.togglePlayback();
    }

    final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            playbackService = ((PlaybackService.LocalBinder)service).getService();
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            playbackService = null;
        }
    };

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        final Station station = (Station) adapter.getItem(position);
        if (playbackService != null)
            playbackService.loadPlaylist(station.getStreams().get(0));
    }
}
