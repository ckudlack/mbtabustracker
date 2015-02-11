package com.ckudlack.mbtabustracker.async;

import android.os.AsyncTask;

import com.ckudlack.mbtabustracker.OttoBusEvent;
import com.ckudlack.mbtabustracker.application.MbtaBusTrackerApplication;
import com.ckudlack.mbtabustracker.database.DBAdapter;
import com.ckudlack.mbtabustracker.database.DatabaseObject;
import com.ckudlack.mbtabustracker.database.Schema;
import com.ckudlack.mbtabustracker.models.RouteStop;

import java.util.ArrayList;
import java.util.List;

public class PersistRouteStopsInDbTask extends AsyncTask<List<RouteStop>, Void, Void> {

    private String routeId;

    public PersistRouteStopsInDbTask(String routeId) {
        this.routeId = routeId;
    }

    @SafeVarargs
    @Override
    protected final Void doInBackground(List<RouteStop>... params) {
        if (params[0] == null) {
            return null;
        }

        List<DatabaseObject> updatedStops = new ArrayList<>();

        DBAdapter dbAdapter = MbtaBusTrackerApplication.getDbAdapter();

        for (RouteStop rs : params[0]) {
            updatedStops.add(rs);
        }

        dbAdapter.batchPersist(updatedStops, Schema.RouteStopsTable.TABLE_NAME);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        MbtaBusTrackerApplication.bus.post(new OttoBusEvent.RouteStopsPersistedEvent(routeId));
    }
}
