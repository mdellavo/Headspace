package org.quuux.headspace.ui;
import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Transformation;

import org.quuux.headspace.R;
import org.quuux.headspace.data.Directory;
import org.quuux.headspace.data.Favorites;
import org.quuux.headspace.data.Station;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.ViewHolder> {

    private static int MODE_DIRECTORY = 0;
    private static int MODE_FAVORITES = 1;

    public interface Listener {
        void onStationClicked(final Station station);
    }

    private final List<Station> stations = new LinkedList<>();
    private Listener listener;
    private int mode;

    public DirectoryAdapter() {
        stations.addAll(getStations());
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.directory_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Station station = stations.get(position);
        holder.nameView.setText(station.getName());
        holder.descriptionView.setText(String.format("%s (%s)", station.getDescription(), station.getNetwork()));

        Picasso.with(holder.itemView.getContext()).load(station.getIconUrl()).fit().centerCrop().transform(new Transformation() {
            @Override
            public Bitmap transform(final Bitmap source) {
                PaletteCache.generate(station.getIconUrl(), source);
                return source;
            }

            @Override
            public String key() {
                return station.getIconUrl();
            }
        }).into(holder.iconView, new Callback() {
            @Override
            public void onSuccess() {
                final Palette palette = PaletteCache.get(station.getIconUrl());
                if (palette == null)
                    return;

                final Palette.Swatch swatch = palette.getVibrantSwatch();
                if (swatch != null) {
                    holder.nameView.setTextColor(swatch.getRgb());
                }
            }

            @Override
            public void onError() {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (listener != null)
                    listener.onStationClicked(station);
            }
        });
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return stations.size();
    }

    private List<Station> getStations() {
        if (mode == MODE_FAVORITES) {
            return Favorites.getFavorites();
        }

        return Directory.getStations();
    }

    public void showFavorites() {
        mode = MODE_FAVORITES;
        updateStations();
    }

    public void showDirectory() {
        mode = MODE_DIRECTORY;
        updateStations();
    }

    public void setListener(final Listener listener) {
        this.listener = listener;
    }


    public void filterStations(final String query) {
        List<Station> filteredStations = queryStations(query);
        updateStations(filteredStations);
    }

    public void updateStations(final List<Station> filteredStations) {
        removeFilteredStations(filteredStations);
        addFilteredStations(filteredStations);
    }

    public void updateStations() {
        updateStations(getStations());
    }

    private void removeFilteredStations(final List<Station> filteredStations) {
        final Iterator<Station> iterator = stations.iterator();
        while (iterator.hasNext()) {
            final Station station = iterator.next();
            if (!filteredStations.contains(station)) {
                final int position = stations.indexOf(station);
                iterator.remove();
                notifyItemRemoved(position);
            }
        }
    }

    private void addFilteredStations(final List<Station> filteredStations) {
        for (Station station : filteredStations) {
            if (!stations.contains(station)) {
                final int position = findPosition(station);
                stations.add(position, station);
                notifyItemInserted(position);

            }
        }
    }

    private int findPosition(final Station station) {

        int position = 0;
        while(position < stations.size() && stations.get(position).getName().compareToIgnoreCase(station.getName()) < 0) {
            position++;
        }

        return position;
    }

    private List<Station> queryStations(final String query) {
        final List<Station> filtered = new ArrayList<>();

        final boolean isEmpty = TextUtils.isEmpty(query);
        for (Station station : getStations()) {
            if (isEmpty || station.matchesQuery(query))
                filtered.add(station);
        }
        return filtered;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iconView;
        TextView nameView, descriptionView;

        public ViewHolder(final View itemView) {
            super(itemView);
            iconView = (ImageView)itemView.findViewById(R.id.icon);
            nameView = (TextView)itemView.findViewById(R.id.name);
            descriptionView = (TextView)itemView.findViewById(R.id.description);
        }
    }
}
