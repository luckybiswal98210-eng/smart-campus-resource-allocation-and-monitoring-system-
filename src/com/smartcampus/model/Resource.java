package com.smartcampus.model;

/**
 * Model representing a Campus Resource (Classroom, Lab, Seminar Hall, Equipment)
 */
public class Resource {
    private int resourceId;
    private String name;
    private String category; // 'SMART_CLASSROOM', 'LABORATORY', 'SEMINAR_HALL', 'EQUIPMENT'
    private String building;
    private String floorRoom;
    private int capacity;
    private double powerRatingKw;
    private String status; // 'AVAILABLE', 'ALLOCATED', 'MAINTENANCE'
    private String features;

    public Resource() {}

    public Resource(int resourceId, String name, String category, String building, String floorRoom, 
                    int capacity, double powerRatingKw, String status, String features) {
        this.resourceId = resourceId;
        this.name = name;
        this.category = category;
        this.building = building;
        this.floorRoom = floorRoom;
        this.capacity = capacity;
        this.powerRatingKw = powerRatingKw;
        this.status = status;
        this.features = features;
    }

    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBuilding() { return building; }
    public void setBuilding(String building) { this.building = building; }

    public String getFloorRoom() { return floorRoom; }
    public void setFloorRoom(String floorRoom) { this.floorRoom = floorRoom; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public double getPowerRatingKw() { return powerRatingKw; }
    public void setPowerRatingKw(double powerRatingKw) { this.powerRatingKw = powerRatingKw; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }

    public boolean isAvailable() {
        return "AVAILABLE".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return name + " [" + category + " - " + building + "]";
    }
}
