package com.smartcampus.model;

import java.sql.Timestamp;

/**
 * Model representing IoT Telemetry / Sensor status for a Resource
 */
public class Telemetry {
    private int logId;
    private int resourceId;
    private Timestamp timestamp;
    private int occupancyCount;
    private double powerDrawKw;
    private double temperatureC;
    private String hvacStatus; // 'ON', 'OFF', 'ECO'
    private String lightStatus; // 'ON', 'OFF', 'DIM'

    // Joined field
    private String resourceName;

    public Telemetry() {}

    public Telemetry(int logId, int resourceId, Timestamp timestamp, int occupancyCount, 
                     double powerDrawKw, double temperatureC, String hvacStatus, String lightStatus) {
        this.logId = logId;
        this.resourceId = resourceId;
        this.timestamp = timestamp;
        this.occupancyCount = occupancyCount;
        this.powerDrawKw = powerDrawKw;
        this.temperatureC = temperatureC;
        this.hvacStatus = hvacStatus;
        this.lightStatus = lightStatus;
    }

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public int getOccupancyCount() { return occupancyCount; }
    public void setOccupancyCount(int occupancyCount) { this.occupancyCount = occupancyCount; }

    public double getPowerDrawKw() { return powerDrawKw; }
    public void setPowerDrawKw(double powerDrawKw) { this.powerDrawKw = powerDrawKw; }

    public double getTemperatureC() { return temperatureC; }
    public void setTemperatureC(double temperatureC) { this.temperatureC = temperatureC; }

    public String getHvacStatus() { return hvacStatus; }
    public void setHvacStatus(String hvacStatus) { this.hvacStatus = hvacStatus; }

    public String getLightStatus() { return lightStatus; }
    public void setLightStatus(String lightStatus) { this.lightStatus = lightStatus; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }
}
