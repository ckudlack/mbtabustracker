package com.ckudlack.mbtabustracker.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.ckudlack.mbtabustracker.R;
import com.ckudlack.mbtabustracker.application.MbtaBusTrackerApplication;
import com.ckudlack.mbtabustracker.models.Stop;

public class StopsAdapter extends CursorAdapter {
    private class ViewHolder {
        TextView stopName;
    }

    public StopsAdapter(Context context, Cursor c) {
        super(context, c);
    }

    public StopsAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    public StopsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final ViewHolder holder;
        View v = View.inflate(context, R.layout.routes_cell_view, null);
        holder = new ViewHolder();
        //noinspection ConstantConditions
        holder.stopName = (TextView) v.findViewById(R.id.item_name);
        v.setTag(holder);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        final Stop stop = new Stop();
        if (cursor != null) {
            stop.buildFromCursor(cursor, MbtaBusTrackerApplication.getDbAdapter());
        } else {
            return;
        }

        holder.stopName.setText(stop.getStopName());
    }

    public void loadNewCursor(Cursor c) {
        this.changeCursor(c);
        this.notifyDataSetChanged();
    }
}
