package com.ckudlack.mbtabustracker.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Direction2 {

    @SerializedName("direction_id")
    @Expose
    private String directionId;
    @SerializedName("direction_name")
    @Expose
    private String directionName;
    @Expose
    private List<Stop> stop = new ArrayList<Stop>();

    /**
     * @return The directionId
     */
    public String getDirectionId() {
        return directionId;
    }

    /**
     * @param directionId The direction_id
     */
    public void setDirectionId(String directionId) {
        this.directionId = directionId;
    }

    /**
     * @return The directionName
     */
    public String getDirectionName() {
        return directionName;
    }

    /**
     * @param directionName The direction_name
     */
    public void setDirectionName(String directionName) {
        this.directionName = directionName;
    }

    /**
     * @return The stop
     */
    public List<Stop> getStop() {
        return stop;
    }

    /**
     * @param stop The stop
     */
    public void setStop(List<Stop> stop) {
        this.stop = stop;
    }

}
