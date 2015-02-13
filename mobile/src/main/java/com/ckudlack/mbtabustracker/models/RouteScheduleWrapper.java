package com.ckudlack.mbtabustracker.models;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RouteScheduleWrapper {

    @SerializedName("route_id")
    @Expose
    private String routeId;
    @SerializedName("route_name")
    @Expose
    private String routeName;
    @Expose
    private List<Direction> direction = new ArrayList<>();

    /**
     *
     * @return
     * The routeId
     */
    public String getRouteId() {
        return routeId;
    }

    /**
     *
     * @param routeId
     * The route_id
     */
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    /**
     *
     * @return
     * The routeName
     */
    public String getRouteName() {
        return routeName;
    }

    /**
     *
     * @param routeName
     * The route_name
     */
    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    /**
     *
     * @return
     * The direction
     */
    public List<Direction> getDirection() {
        return direction;
    }

    /**
     *
     * @param direction
     * The direction
     */
    public void setDirection(List<Direction> direction) {
        this.direction = direction;
    }

}