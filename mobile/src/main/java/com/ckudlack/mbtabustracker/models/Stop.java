package com.ckudlack.mbtabustracker.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.ckudlack.mbtabustracker.database.DBAdapter;
import com.ckudlack.mbtabustracker.database.DatabaseObject;
import com.ckudlack.mbtabustracker.database.Schema;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Stop extends DatabaseObject {

    @SerializedName("stop_order")
    @Expose
    private String stopOrder;
    @SerializedName("stop_id")
    @Expose
    private String stopId;
    @SerializedName("stop_name")
    @Expose
    private String stopName;
    @SerializedName("parent_station")
    @Expose
    private String parentStation;
    @SerializedName("parent_station_name")
    @Expose
    private String parentStationName;
    @SerializedName("stop_lat")
    @Expose
    private String stopLat;
    @SerializedName("stop_lon")
    @Expose
    private String stopLon;
    @Expose
    private String distance;

    /**
     * @return The stopOrder
     */
    public String getStopOrder() {
        return stopOrder;
    }

    /**
     * @param stopOrder The stop_order
     */
    public void setStopOrder(String stopOrder) {
        this.stopOrder = stopOrder;
    }

    /**
     * @return The stopId
     */
    public String getStopId() {
        return stopId;
    }

    /**
     * @param stopId The stop_id
     */
    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    /**
     * @return The stopName
     */
    public String getStopName() {
        return stopName;
    }

    /**
     * @param stopName The stop_name
     */
    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    /**
     * @return The parentStation
     */
    public String getParentStation() {
        return parentStation;
    }

    /**
     * @param parentStation The parent_station
     */
    public void setParentStation(String parentStation) {
        this.parentStation = parentStation;
    }

    /**
     * @return The parentStationName
     */
    public String getParentStationName() {
        return parentStationName;
    }

    /**
     * @param parentStationName The parent_station_name
     */
    public void setParentStationName(String parentStationName) {
        this.parentStationName = parentStationName;
    }

    /**
     * @return The stopLat
     */
    public String getStopLat() {
        return stopLat;
    }

    /**
     * @param stopLat The stop_lat
     */
    public void setStopLat(String stopLat) {
        this.stopLat = stopLat;
    }

    /**
     * @return The stopLon
     */
    public String getStopLon() {
        return stopLon;
    }

    /**
     * @param stopLon The stop_lon
     */
    public void setStopLon(String stopLon) {
        this.stopLon = stopLon;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    @Override
    protected String getTableName() {
        return Schema.StopsTable.TABLE_NAME;
    }

    @Override
    protected void buildSubclassFromCursor(Cursor cursor, DBAdapter dbAdapter) {
        setStopId(cursor.getString(cursor.getColumnIndex(Schema.StopsTable.STOP_ID)));
        setStopName(cursor.getString(cursor.getColumnIndex(Schema.StopsTable.STOP_NAME)));
        setStopLat(cursor.getString(cursor.getColumnIndex(Schema.StopsTable.STOP_LAT)));
        setStopLon(cursor.getString(cursor.getColumnIndex(Schema.StopsTable.STOP_LONG)));
        setParentStation(cursor.getString(cursor.getColumnIndex(Schema.StopsTable.STOP_PARENT_STATION)));
        setParentStationName(cursor.getString(cursor.getColumnIndex(Schema.StopsTable.STOP_PARENT_STATION_NAME)));
        setStopOrder(cursor.getString(cursor.getColumnIndex(Schema.StopsTable.STOP_ORDER)));
    }

    @Override
    public void fillInContentValues(ContentValues values, DBAdapter dbAdapter) {
        contentValuesHelper(values);
    }

    public void contentValuesHelper(ContentValues values) {
        values.put(Schema.StopsTable.STOP_ID, stopId);
        values.put(Schema.StopsTable.STOP_LAT, stopLat);
        values.put(Schema.StopsTable.STOP_LONG, stopLon);
        values.put(Schema.StopsTable.STOP_NAME, stopName);
        values.put(Schema.StopsTable.STOP_ORDER, stopOrder);
        values.put(Schema.StopsTable.STOP_PARENT_STATION, parentStation);
        values.put(Schema.StopsTable.STOP_PARENT_STATION_NAME, parentStationName);
    }
}