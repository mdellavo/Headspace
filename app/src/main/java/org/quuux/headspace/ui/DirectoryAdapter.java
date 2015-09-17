package org.quuux.headspace.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import org.quuux.headspace.R;
import org.quuux.headspace.data.Directory;
import org.quuux.headspace.data.Station;

import java.util.List;

public class DirectoryAdapter extends RecyclerView.Adapter<DirectoryAdapter.ViewHolder> {

    private Listener listener;

    public interface Listener {
        void onStationClicked(final Station station);
    }

    private final List<Station> stations;

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
        Picasso.with(holder.itemView.getContext()).load(station.getIconUrl()).fit().centerCrop().into(holder.iconView);
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
