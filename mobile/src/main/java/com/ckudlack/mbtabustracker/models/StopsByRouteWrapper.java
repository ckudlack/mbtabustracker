package com.ckudlack.mbtabustracker.models;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

public class StopsByRouteWrapper {

    @Expose
    private List<Direction2> direction = new ArrayList<>();

    /**
     * @return The direction
     */
    public List<Direction2> getDirection() {
        return direction;
    }

    /**
     * @param direction The direction
     */
    public void setDirection(List<Direction2> direction) {
        this.direction = direction;
    }

}