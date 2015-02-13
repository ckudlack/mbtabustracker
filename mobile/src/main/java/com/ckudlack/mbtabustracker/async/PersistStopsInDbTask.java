package com.ckudlack.mbtabustracker.async;

import android.content.ContentValues;
import android.os.AsyncTask;

import com.ckudlack.mbtabustracker.OttoBusEvent;
import com.ckudlack.mbtabustracker.application.MbtaBusTrackerApplication;
import com.ckudlack.mbtabustracker.database.DBAdapter;
import com.ckudlack.mbtabustracker.database.DatabaseObject;
import com.ckudlack.mbtabustracker.database.Schema;
import com.ckudlack.mbtabustracker.models.Stop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PersistStopsInDbTask extends AsyncTask<List<Stop>, Void, Void> {

    private String routeId;
    private String direction;
    private List<Stop> stops;

    public PersistStopsInDbTask(String routeId, String direction) {
        this.routeId = routeId;
        this.direction = direction;
    }

    @SafeVarargs
    @Override
    protected final Void doInBackground(List<Stop>... params) {
        if (params[0] == null) {
            return null;
        }

        stops = params[0];

        List<DatabaseObject> updatedStops = new ArrayList<>();

        DBAdapter dbAdapter = MbtaBusTrackerApplication.getDbAdapter();

        HashMap<String, String> stopsWithDirection = dbAdapter.getIdToDirectionMap(Schema.StopsTable.TABLE_NAME, Schema.StopsTable.ALL_COLUMNS, Schema.StopsTable.STOP_ID, Schema.StopsTable.STOP_DIRECTION);

        for (Stop s : params[0]) {
            s.setDirection(direction);
            if (stopsWithDirection.containsKey(s.getStopId()) && stopsWithDirection.get(s.getStopId()).equals(s.getDirection())) {
                ContentValues values = new ContentValues();
                s.contentValuesHelper(values);

                dbAdapter.db.update(Schema.StopsTable.TABLE_NAME, values, Schema.StopsTable.STOP_ID + " = " + s.getStopId(), null);
            } else {
                updatedStops.add(s);
            }
        }

        dbAdapter.batchPersist(updatedStops, Schema.StopsTable.TABLE_NAME);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        MbtaBusTrackerApplication.bus.post(new OttoBusEvent.StopsPersistedEvent(routeId, stops));
    }
}
