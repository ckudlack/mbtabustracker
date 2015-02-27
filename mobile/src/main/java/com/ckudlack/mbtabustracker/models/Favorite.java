package com.ckudlack.mbtabustracker.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.ckudlack.mbtabustracker.database.DBAdapter;
import com.ckudlack.mbtabustracker.database.DatabaseObject;
import com.ckudlack.mbtabustracker.database.Schema;

public class Favorite extends DatabaseObject {

    String routeId;
    String stopId;
    String directionId;
    String directionName;
    String routeName;
    String stopName;
    String predictions;
    String order;
    double longitude;
    double latitude;

    public Favorite() {
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

    public String getDirectionId() {
        return directionId;
    }

    public void setDirectionId(String directionId) {
        this.directionId = directionId;
    }

    public String getDirectionName() {
        return directionName;
    }

    public void setDirectionName(String directionName) {
        this.directionName = directionName;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    public String getPredictions() {
        return predictions;
    }

    public void setPredictions(String predictions) {
        this.predictions = predictions;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    protected String getTableName() {
        return Schema.FavoritesTable.TABLE_NAME;
    }

    @Override
    protected void buildSubclassFromCursor(Cursor cursor, DBAdapter dbAdapter) {
        setDirectionId(cursor.getString(cursor.getColumnIndex(Schema.FavoritesTable.DIRECTION_ID)));
        setDirectionName(cursor.getString(cursor.getColumnIndex(Schema.FavoritesTable.DIRECTION_NAME)));
        setStopId(cursor.getString(cursor.getColumnIndex(Schema.FavoritesTable.STOP_ID)));
        setStopName(cursor.getString(cursor.getColumnIndex(Schema.FavoritesTable.STOP_NAME)));
        setLatitude(cursor.getDouble(cursor.getColumnIndex(Schema.FavoritesTable.STOP_LAT)));
        setLongitude(cursor.getDouble(cursor.getColumnIndex(Schema.FavoritesTable.STOP_LONG)));
        setRouteId(cursor.getString(cursor.getColumnIndex(Schema.FavoritesTable.ROUTE_ID)));
        setRouteName(cursor.getString(cursor.getColumnIndex(Schema.FavoritesTable.ROUTE_NAME)));
        setOrder(cursor.getString(cursor.getColumnIndex(Schema.FavoritesTable.STOP_ORDER)));
        setPredictions(cursor.getString(cursor.getColumnIndex(Schema.FavoritesTable.PREDICTIONS)));
    }

    @Override
    public void fillInContentValues(ContentValues values, DBAdapter dbAdapter) {
        values.put(Schema.FavoritesTable.DIRECTION_ID, directionId);
        values.put(Schema.FavoritesTable.DIRECTION_NAME, directionName);
        values.put(Schema.FavoritesTable.PREDICTIONS, predictions);
        values.put(Schema.FavoritesTable.ROUTE_ID, routeId);
        values.put(Schema.FavoritesTable.ROUTE_NAME, routeName);
        values.put(Schema.FavoritesTable.STOP_ID, stopId);
        values.put(Schema.FavoritesTable.STOP_NAME, stopName);
        values.put(Schema.FavoritesTable.STOP_LAT, latitude);
        values.put(Schema.FavoritesTable.STOP_LONG, longitude);
        values.put(Schema.FavoritesTable.STOP_ORDER, order);
    }
}
