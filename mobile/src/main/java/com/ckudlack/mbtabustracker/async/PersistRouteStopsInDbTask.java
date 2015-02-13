package com.ckudlack.mbtabustracker.async;

import android.content.ContentValues;
import android.os.AsyncTask;

import com.ckudlack.mbtabustracker.OttoBusEvent;
import com.ckudlack.mbtabustracker.application.MbtaBusTrackerApplication;
import com.ckudlack.mbtabustracker.database.DBAdapter;
import com.ckudlack.mbtabustracker.database.DatabaseObject;
import com.ckudlack.mbtabustracker.database.Schema;
import com.ckudlack.mbtabustracker.models.RouteStop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PersistRouteStopsInDbTask extends AsyncTask<List<RouteStop>, Void, Void> {

    private String routeId;
    private String direction;

    public PersistRouteStopsInDbTask(String routeId, String direction) {
        this.routeId = routeId;
        this.direction = direction;
    }

    @SafeVarargs
    @Override
    protected final Void doInBackground(List<RouteStop>... params) {
        if (params[0] == null) {
            return null;
        }

        List<DatabaseObject> updatedStops = new ArrayList<>();

        DBAdapter dbAdapter = MbtaBusTrackerApplication.getDbAdapter();

        HashMap<String, String> idToDirectionMap = dbAdapter.getIdToDirectionMap(Schema.RouteStopsTable.TABLE_NAME, Schema.RouteStopsTable.ALL_COLUMNS, Schema.RouteStopsTable.ROUTE_ID, Schema.RouteStopsTable.DIRECTION);

        for (RouteStop rs : params[0]) {
            if (idToDirectionMap.containsKey(routeId) && idToDirectionMap.get(routeId).equals(direction)) {
                ContentValues values = new ContentValues();
                rs.fillInContentValues(values, dbAdapter);

                dbAdapter.db.update(Schema.RouteStopsTable.TABLE_NAME, values, Schema.RouteStopsTable.ROUTE_ID + " = " + rs.getRouteId(), null);
            } else {
                updatedStops.add(rs);
            }
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
