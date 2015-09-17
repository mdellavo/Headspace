package org.quuux.headspace.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.telecom.Call;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Transformation;

import org.quuux.headspace.R;
import org.quuux.headspace.data.Directory;
import org.quuux.headspace.data.Station;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.ViewHolder> {

    private Listener listener;

    public interface Listener {
        void onStationClicked(final Station station);
    }

    private final List<Station> stations;
    private Map<String, Palette> paletteCache = new HashMap<>();

    public DirectoryAdapter(final Context context) {
        stations = Directory.getStations(context);
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
        holder.descriptionView.setText(station.getDescription());

        Picasso.with(holder.itemView.getContext()).load(station.getIconUrl()).fit().centerCrop().transform(new Transformation() {
            @Override
            public Bitmap transform(final Bitmap source) {

                if (!paletteCache.containsKey(station.getIconUrl())) {
                    final Palette palette = Palette.from(source).generate();
                    paletteCache.put(station.getIconUrl(), palette);
                }

                return source;
            }

            @Override
            public String key() {
                return station.getIconUrl();
            }
        }).into(holder.iconView, new Callback() {
            @Override
            public void onSuccess() {
                final Palette palette = paletteCache.get(station.getIconUrl());
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

    public void setListener(final Listener listener) {
        this.listener = listener;
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
