package com.ckudlack.mbtabustracker.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import com.ckudlack.mbtabustracker.database.DBAdapter;
import com.ckudlack.mbtabustracker.database.DatabaseObject;
import com.ckudlack.mbtabustracker.database.Schema;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Route extends DatabaseObject {

    @SerializedName("route_id")
    @Expose
    private String routeId;
    @SerializedName("route_name")
    @Expose
    private String routeName;
    @Expose
    private List<Direction> direction = new ArrayList<Direction>();

    List<Stop> stops = new ArrayList<>();

    String[] stopIds;

    /**
     * @return The routeId
     */
    public String getRouteId() {
        return routeId;
    }

    /**
     * @param routeId The route_id
     */
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    /**
     * @return The routeName
     */
    public String getRouteName() {
        return routeName;
    }

    /**
     * @param routeName The route_name
     */
    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    /**
     * @return The direction
     */
    public List<Direction> getDirection() {
        return direction;
    }

    /**
     * @param direction The direction
     */
    public void setDirection(List<Direction> direction) {
        this.direction = direction;
    }

    public String[] getStopIds() {
        return stopIds;
    }

    @Override
    protected String getTableName() {
        return Schema.RoutesTable.TABLE_NAME;
    }

    @Override
    protected void buildSubclassFromCursor(Cursor cursor, DBAdapter dbAdapter) {
        setRouteId(cursor.getString(cursor.getColumnIndex(Schema.RoutesTable.ROUTE_ID)));
        setRouteName(cursor.getString(cursor.getColumnIndex(Schema.RoutesTable.ROUTE_NAME)));

        String idsList = cursor.getString(cursor.getColumnIndex(Schema.RoutesTable.STOP_IDS_LIST));
        if (!TextUtils.isEmpty(idsList)) {
            String[] ids = idsList.split(" ");
            stopIds = ids.clone();
            stops = dbAdapter.getStopsForRoute(this);
        }
    }

    @Override
    public void fillInContentValues(ContentValues values, DBAdapter dbAdapter) {
        contentValuesHelper(values);
    }

    public void contentValuesHelper(ContentValues values) {
        values.put(Schema.RoutesTable.ROUTE_ID, routeId);
        values.put(Schema.RoutesTable.ROUTE_NAME, routeName);
    }

}