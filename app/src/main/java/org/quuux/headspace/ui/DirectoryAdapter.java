package org.quuux.headspace.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.quuux.headspace.R;
import org.quuux.headspace.data.Directory;
import org.quuux.headspace.data.Station;

import java.util.List;

public class DirectoryAdapter extends BaseAdapter {

    private final List<Station> stations;

    private static final class Holder {
        ImageView iconView;
        TextView nameView, descriptionView;
    }

    public DirectoryAdapter(final Context context) {
        stations = Directory.getStations(context);
    }

    @Override
    public int getCount() {
        return stations.size();
    }

    @Override
    public Object getItem(final int position) {
        return stations.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final View view = convertView != null ? convertView : newView(parent);
        bindView(view, (Station)getItem(position));
        return view;
    }

    private void bindView(final View view, final Station station) {
        final Holder holder = (Holder) view.getTag();
        holder.nameView.setText(station.getName());
        holder.descriptionView.setText(station.getDescription());
        Picasso.with(view.getContext()).load(station.getIconUrl()).fit().centerCrop().into(holder.iconView);
    }

    private View newView(final ViewGroup parent) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.directory_row, parent, false);
        final Holder holder = new Holder();
        holder.iconView = (ImageView)view.findViewById(R.id.icon);
        holder.nameView = (TextView)view.findViewById(R.id.name);
        holder.descriptionView = (TextView)view.findViewById(R.id.description);
        view.setTag(holder);
        return view;
    }
}
