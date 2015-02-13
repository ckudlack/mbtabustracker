package com.ckudlack.mbtabustracker.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class StopTime {

    @SerializedName("stop_sequence")
    @Expose
    private String stopSequence;
    @SerializedName("stop_id")
    @Expose
    private String stopId;
    @SerializedName("stop_name")
    @Expose
    private String stopName;
    @SerializedName("sch_arr_dt")
    @Expose
    private String schArrDt;
    @SerializedName("sch_dep_dt")
    @Expose
    private String schDepDt;

    /**
     * @return The stopSequence
     */
    public String getStopSequence() {
        return stopSequence;
    }

    /**
     * @param stopSequence The stop_sequence
     */
    public void setStopSequence(String stopSequence) {
        this.stopSequence = stopSequence;
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
     * @return The schArrDt
     */
    public String getSchArrDt() {
        return schArrDt;
    }

    /**
     * @param schArrDt The sch_arr_dt
     */
    public void setSchArrDt(String schArrDt) {
        this.schArrDt = schArrDt;
    }

    /**
     * @return The schDepDt
     */
    public String getSchDepDt() {
        return schDepDt;
    }

    /**
     * @param schDepDt The sch_dep_dt
     */
    public void setSchDepDt(String schDepDt) {
        this.schDepDt = schDepDt;
    }

}
