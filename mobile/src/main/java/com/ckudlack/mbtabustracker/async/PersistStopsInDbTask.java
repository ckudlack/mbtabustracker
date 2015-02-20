package com.ckudlack.mbtabustracker.async;

import android.os.AsyncTask;

import com.ckudlack.mbtabustracker.OttoBusEvent;
import com.ckudlack.mbtabustracker.application.MbtaBusTrackerApplication;
import com.ckudlack.mbtabustracker.database.DBAdapter;
import com.ckudlack.mbtabustracker.database.DatabaseObject;
import com.ckudlack.mbtabustracker.database.Schema;
import com.ckudlack.mbtabustracker.models.Stop;

import java.util.ArrayList;
import java.util.List;

public class PersistStopsInDbTask extends AsyncTask<List<Stop>, Void, List<Long>> {

    private String routeId;
    private String direction;
    private List<Stop> stops;

    public PersistStopsInDbTask(String routeId, String direction) {
        this.routeId = routeId;
        this.direction = direction;
    }

    @SafeVarargs
    @Override
    protected final List<Long> doInBackground(List<Stop>... params) {
        if (params[0] == null) {
            return null;
        }

        stops = params[0];

        List<DatabaseObject> updatedStops = new ArrayList<>();

        DBAdapter dbAdapter = MbtaBusTrackerApplication.getDbAdapter();

        for (Stop s : params[0]) {
            s.setDirection(direction);
            updatedStops.add(s);
        }

        return dbAdapter.batchPersistAndReturnIds(updatedStops, Schema.StopsTable.TABLE_NAME);
    }

    @Override
    protected void onPostExecute(List<Long> ids) {
        super.onPostExecute(ids);
        MbtaBusTrackerApplication.bus.post(new OttoBusEvent.StopsPersistedEvent(routeId, ids));
    }
}
