package com.ckudlack.mbtabustracker.async;

import android.content.ContentValues;
import android.os.AsyncTask;

import com.ckudlack.mbtabustracker.OttoBusEvent;
import com.ckudlack.mbtabustracker.application.MbtaBusTrackerApplication;
import com.ckudlack.mbtabustracker.database.DBAdapter;
import com.ckudlack.mbtabustracker.database.DatabaseObject;
import com.ckudlack.mbtabustracker.database.Schema;
import com.ckudlack.mbtabustracker.models.Route;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PersistRoutesInDbTask extends AsyncTask<List<Route>, Void, Void> {

    @SafeVarargs
    @Override
    protected final Void doInBackground(List<Route>... params) {
        if (params[0] == null) {
            return null;
        }

        List<DatabaseObject> updatedRoutes = new ArrayList<>();

        DBAdapter dbAdapter = MbtaBusTrackerApplication.getDbAdapter();

        // Get previous IDs
        Set<String> ids = dbAdapter.getAllIds(Schema.RoutesTable.TABLE_NAME, Schema.RoutesTable.ALL_COLUMNS, Schema.RoutesTable.ROUTE_ID);

        for (Route r : params[0]) {
            if (ids.contains(r.getRouteId())) {
                ContentValues values = new ContentValues();
                r.contentValuesHelper(values);

                dbAdapter.db.update(Schema.RoutesTable.TABLE_NAME, values,
                        Schema.RoutesTable.ROUTE_ID + " = " + r.getRouteId(), null);
/*                dbAdapter.db.execSQL("UPDATE " + Schema.RoutesTable.TABLE_NAME + " SET "
                        + Schema.UserTable.DB_CATEGORY + " = " + Schema.UserTable.DB_CATEGORY + " *" + dbCategory + " WHERE ("
                        + Schema.UserTable.ACCOUNT_ID + ") = " + user.getId() + " AND (" + Schema.UserTable.DB_CATEGORY + " % " + dbCategory + " != 0)");*/
            } else {
                updatedRoutes.add(r);
            }
        }

        dbAdapter.batchPersist(updatedRoutes, Schema.RoutesTable.TABLE_NAME);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        MbtaBusTrackerApplication.bus.post(new OttoBusEvent.DbPersistCompletedEvent());
    }
}
