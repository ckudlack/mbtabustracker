package com.ckudlack.mbtabustracker.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

public class AllRoutesWrapper {

    @Expose
    private List<Mode> mode = new ArrayList<Mode>();

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

}