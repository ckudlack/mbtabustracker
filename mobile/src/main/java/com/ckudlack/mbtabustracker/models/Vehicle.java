package com.ckudlack.mbtabustracker.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Vehicle {

    @SerializedName("vehicle_id")
    @Expose
    private String vehicleId;
    @SerializedName("vehicle_lat")
    @Expose
    private String vehicleLat;
    @SerializedName("vehicle_lon")
    @Expose
    private String vehicleLon;
    @SerializedName("vehicle_timestamp")
    @Expose
    private String vehicleTimestamp;

    /**
     * @return The vehicleId
     */
    public String getVehicleId() {
        return vehicleId;
    }

    /**
     * @param vehicleId The vehicle_id
     */
    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    /**
     * @return The vehicleLat
     */
    public String getVehicleLat() {
        return vehicleLat;
    }

    /**
     * @param vehicleLat The vehicle_lat
     */
    public void setVehicleLat(String vehicleLat) {
        this.vehicleLat = vehicleLat;
    }

    /**
     * @return The vehicleLon
     */
    public String getVehicleLon() {
        return vehicleLon;
    }

    /**
     * @param vehicleLon The vehicle_lon
     */
    public void setVehicleLon(String vehicleLon) {
        this.vehicleLon = vehicleLon;
    }

    /**
     * @return The vehicleTimestamp
     */
    public String getVehicleTimestamp() {
        return vehicleTimestamp;
    }

    /**
     * @param vehicleTimestamp The vehicle_timestamp
     */
    public void setVehicleTimestamp(String vehicleTimestamp) {
        this.vehicleTimestamp = vehicleTimestamp;
    }

}