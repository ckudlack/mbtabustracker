package com.ckudlack.mbtabustracker.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ckudlack.mbtabustracker.R;
import com.ckudlack.mbtabustracker.models.Favorite;

import java.util.List;


/**
 * This should be extending CursorAdapter, but since I made it this way originally because data was coming
 * from shared preferences, and later changed to storing it in the DB, I'm not going to change it.
 */
public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    public interface ItemClickedCallback {
        public void onListItemClicked(int position);

        public void onCloseButtonClicked(int position);
    }

    List<Favorite> favorites;
    ItemClickedCallback callback;

    public FavoritesAdapter(List<Favorite> favorites, ItemClickedCallback callback) {
        this.favorites = favorites;
        this.callback = callback;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView routeName;
        TextView predictionTimes;
        TextView stopName;
        RelativeLayout rootView;
        ImageButton closeButton;

        public ViewHolder(View v) {
            super(v);

            //TODO: More views
            routeName = (TextView) v.findViewById(R.id.title);
            predictionTimes = (TextView) v.findViewById(R.id.prediction_times);
            stopName = (TextView) v.findViewById(R.id.stop_name);
            rootView = (RelativeLayout) v.findViewById(R.id.card_root_view);
            closeButton = (ImageButton) v.findViewById(R.id.close_button);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int i) {
        Favorite favorite = favorites.get(i);
        viewHolder.routeName.setText("Route " + favorite.getRouteName());
        viewHolder.stopName.setText(favorite.getStopName());
        viewHolder.predictionTimes.setText(favorite.getPredictions());
        viewHolder.rootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onListItemClicked(i);
            }
        });
        viewHolder.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onCloseButtonClicked(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }

    public void updateList(List<Favorite> favorites) {
        this.favorites = favorites;
        notifyDataSetChanged();
    }
}
