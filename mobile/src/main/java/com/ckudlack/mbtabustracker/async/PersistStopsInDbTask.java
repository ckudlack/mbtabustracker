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
import java.util.List;
import java.util.Set;

public class PersistStopsInDbTask extends AsyncTask<List<Stop>, Void, Void> {

    private String routeId;

    public PersistStopsInDbTask(String routeId) {
        this.routeId = routeId;
    }

    @Override
    protected Void doInBackground(List<Stop>... params) {
        if (params[0] == null) {
            return null;
        }

        List<DatabaseObject> updatedRoutes = new ArrayList<>();

        DBAdapter dbAdapter = MbtaBusTrackerApplication.getDbAdapter();

        // Get previous IDs
        Set<String> ids = dbAdapter.getAllIds(Schema.StopsTable.TABLE_NAME, Schema.StopsTable.ALL_COLUMNS, Schema.StopsTable.STOP_ID);

        for (Stop s : params[0]) {
            if (ids.contains(s.getStopId())) {
                ContentValues values = new ContentValues();
                s.contentValuesHelper(values);

                dbAdapter.db.update(Schema.StopsTable.TABLE_NAME, values, Schema.StopsTable.STOP_ID + " = " + s.getStopId(), null);
            } else {
                updatedRoutes.add(s);
            }
        }

        dbAdapter.batchPersist(updatedRoutes, Schema.StopsTable.TABLE_NAME);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        MbtaBusTrackerApplication.bus.post(new OttoBusEvent.StopsPersistCompletedEvent(routeId));
    }
}
