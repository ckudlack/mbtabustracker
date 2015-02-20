package com.ckudlack.mbtabustracker.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class VehicleInfoWrapper {

    @SerializedName("route_id")
    @Expose
    private String routeId;
    @SerializedName("route_name")
    @Expose
    private String routeName;
    @SerializedName("route_type")
    @Expose
    private String routeType;
    @SerializedName("mode_name")
    @Expose
    private String modeName;
    @Expose
    private List<Direction> direction = new ArrayList<Direction>();

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
     * @return The routeType
     */
    public String getRouteType() {
        return routeType;
    }

    /**
     * @param routeType The route_type
     */
    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    /**
     * @return The modeName
     */
    public String getModeName() {
        return modeName;
    }

    /**
     * @param modeName The mode_name
     */
    public void setModeName(String modeName) {
        this.modeName = modeName;
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

}