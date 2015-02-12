package com.ckudlack.mbtabustracker.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ckudlack.mbtabustracker.R;
import com.ckudlack.mbtabustracker.models.Favorite;

import java.util.List;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {

    List<Favorite> favorites;

    public FavoritesAdapter(List<Favorite> favorites) {
        this.favorites = favorites;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView routeName;
        TextView predictionTimes;
        TextView stopName;


        public ViewHolder(View v) {
            super(v);

            //TODO: More views
            routeName = (TextView) v.findViewById(R.id.title);
            predictionTimes = (TextView) v.findViewById(R.id.prediction_times);
            stopName = (TextView) v.findViewById(R.id.stop_name);
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
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        Favorite favorite = favorites.get(i);
        viewHolder.routeName.setText("Route " + favorite.getRouteName());
        viewHolder.stopName.setText(favorite.getStopName());
        viewHolder.predictionTimes.setText(favorite.getPredictions());
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
