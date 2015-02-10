package com.ckudlack.mbtabustracker.models;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class StopsNearMeWrapper {

    @Expose
    private List<Stop> stop = new ArrayList<>();

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
