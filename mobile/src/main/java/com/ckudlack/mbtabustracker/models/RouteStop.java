package com.ckudlack.mbtabustracker.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.ckudlack.mbtabustracker.database.DBAdapter;
import com.ckudlack.mbtabustracker.database.DatabaseObject;
import com.ckudlack.mbtabustracker.database.Schema;

/**
    Foreign Key Mapper
*/

public class RouteStop extends DatabaseObject {
    private String routeId;
    private String stopId;

    public RouteStop() {
    }

    public RouteStop(String routeId, String stopId) {
        this.routeId = routeId;
        this.stopId = stopId;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    @Override
    protected String getTableName() {
        return Schema.RouteStopsTable.TABLE_NAME;
    }

    @Override
    protected void buildSubclassFromCursor(Cursor cursor, DBAdapter dbAdapter) {
        setRouteId(cursor.getString(cursor.getColumnIndex(Schema.RouteStopsTable.ROUTE_ID)));
        setStopId(cursor.getString(cursor.getColumnIndex(Schema.RouteStopsTable.STOP_ID)));
    }

    @Override
    public void fillInContentValues(ContentValues values, DBAdapter dbAdapter) {
        values.put(Schema.RouteStopsTable.ROUTE_ID, routeId);
        values.put(Schema.RouteStopsTable.STOP_ID, stopId);
    }

}
