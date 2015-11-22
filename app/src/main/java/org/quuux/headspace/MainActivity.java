package org.quuux.headspace;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.SwipeDismissBehavior;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import org.quuux.feller.Log;
import org.quuux.headspace.data.Station;
import org.quuux.headspace.events.EventBus;
import org.quuux.headspace.events.PlayerError;
import org.quuux.headspace.events.PlayerStateChange;
import org.quuux.headspace.net.Streamer;
import org.quuux.headspace.ui.DirectoryAdapter;
import org.quuux.headspace.ui.PlayerView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, DirectoryAdapter.Listener, SearchView.OnQueryTextListener, PlayerView.Listener {

    private static final String TAG = Log.buildTag(MainActivity.class);

    private PlaybackService playbackService = null;
    private RecyclerView directory;
    private PlayerView playerView;
    private DirectoryAdapter adapter;
    private CoordinatorLayout container;
    private SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startService(new Intent(this, PlaybackService.class));

        setContentView(R.layout.activity_main);

        container = (CoordinatorLayout)findViewById(R.id.container);

        directory = (RecyclerView)findViewById(R.id.directory);
        directory.setHasFixedSize(true);
        directory.setLayoutManager(new GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false));
        directory.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    final InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

                    if (imm.isAcceptingText()) {
                        imm.hideSoftInputFromWindow(directory.getWindowToken(), 0);
                    }
                }
            }
        });

        adapter = new DirectoryAdapter();
        directory.setAdapter(adapter);
        adapter.setListener(this);

        playerView = (PlayerView)findViewById(R.id.player);
        playerView.setListener(this);

        onPlayerStateChanged(null);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) playerView.getLayoutParams();
        final SwipeDismissBehavior<PlayerView> swipeDismissBehavior = new SwipeDismissBehavior<>();
        swipeDismissBehavior.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END);
        swipeDismissBehavior.setListener(new SwipeDismissBehavior.OnDismissListener() {
            @Override
            public void onDismiss(final View view) {
                stopPlayback();
            }

            @Override
            public void onDragStateChanged(final int i) {

            }
        });
        params.setBehavior(swipeDismissBehavior);

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
    public void onBackPressed() {
        if (searchView.isIconified()) {
            super.onBackPressed();
        } else {
            searchView.setQuery(null, true);
            searchView.clearFocus();
            searchView.setIconified(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final boolean rv;
        switch (item.getItemId()) {
            case R.id.action_settings:
                rv = true;
                break;

            case R.id.action_favorites:
                showFavorites();
                rv = true;
                break;

            default:
                rv = super.onOptionsItemSelected(item);
                break;
        }

        return rv;
    }

    private void showFavorites() {
        adapter.showFavorites();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.playback:
                togglePlayback();
                break;
        }
    }

    @Override
    public boolean onQueryTextChange(String query) {
        adapter.filterStations(query);
        directory.scrollToPosition(0);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public void onStationClicked(final Station station) {
        playerView.setTranslationX(0);
        playerView.setAlpha(1);

        if (playbackService != null)
            playbackService.loadStation(station);
    }

    @Subscribe
    public void onPlayerStateChanged(final PlayerStateChange update) {
        final Streamer streamer = Streamer.getInstance();
        final boolean isPlaying = !streamer.isStopped();
        playerView.setVisibility(isPlaying ? View.VISIBLE : View.GONE);
    }

    @Subscribe
    public void onError(final PlayerError error) {
        Log.e(TAG, "player error", error.error);
        Toast.makeText(this, error.error.toString(), Toast.LENGTH_LONG).show();
    }

    public void togglePlayback() {
        if (playbackService != null)
            playbackService.togglePlayback();
    }

    private void stopPlayback() {
        if (playbackService != null)
            playbackService.stopPlayback();
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
    public void openStation(final Station station) {
        final Intent intent = StationActivity.getInstance(this, Streamer.getInstance().getStation());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in, 0);
    }
}
