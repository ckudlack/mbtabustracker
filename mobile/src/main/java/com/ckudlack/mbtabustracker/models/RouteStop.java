package com.ckudlack.mbtabustracker.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.ckudlack.mbtabustracker.database.DBAdapter;
import com.ckudlack.mbtabustracker.database.DatabaseObject;
import com.ckudlack.mbtabustracker.database.Schema;

/**
 * Foreign Key Mapper
 */

public class RouteStop extends DatabaseObject {

    private String routeId;
    private String stopId;
    private String direction;

    public RouteStop() {
    }

    public RouteStop(String routeId, String stopId, String direction) {
        this.routeId = routeId;
        this.stopId = stopId;
        this.direction = direction;
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

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    @Override
    protected String getTableName() {
        return Schema.RouteStopsTable.TABLE_NAME;
    }

    @Override
    protected void buildSubclassFromCursor(Cursor cursor, DBAdapter dbAdapter) {
        setRouteId(cursor.getString(cursor.getColumnIndex(Schema.RouteStopsTable.ROUTE_ID)));
        setStopId(cursor.getString(cursor.getColumnIndex(Schema.RouteStopsTable.STOP_ID)));
        setDirection(cursor.getString(cursor.getColumnIndex(Schema.RouteStopsTable.DIRECTION)));
    }

    @Override
    public void fillInContentValues(ContentValues values, DBAdapter dbAdapter) {
        values.put(Schema.RouteStopsTable.ROUTE_ID, routeId);
        values.put(Schema.RouteStopsTable.STOP_ID, stopId);
        values.put(Schema.RouteStopsTable.DIRECTION, direction);
    }
}
