package com.ckudlack.mbtabustracker.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StopPredictionWrapper {

    @SerializedName("stop_id")
    @Expose
    private String stopId;
    @SerializedName("stop_name")
    @Expose
    private String stopName;
    @Expose
    private List<Mode> mode = new ArrayList<>();
    @SerializedName("alert_headers")
    @Expose
    private List<Object> alertHeaders = new ArrayList<>();

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
     * @return The mode
     */
    public List<Mode> getMode() {
        return mode;
    }

    /**
     * @param mode The mode
     */
    public void setMode(List<Mode> mode) {
        this.mode = mode;
    }

    /**
     * @return The alertHeaders
     */
    public List<Object> getAlertHeaders() {
        return alertHeaders;
    }

    /**
     * @param alertHeaders The alert_headers
     */
    public void setAlertHeaders(List<Object> alertHeaders) {
        this.alertHeaders = alertHeaders;
    }

}