package com.smartcampus.model;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

/**
 * Model representing a Resource Allocation/Booking record
 */
public class Allocation {
    private int allocationId;
    private int resourceId;
    private int userId;
    private String purpose;
    private Date bookingDate;
    private Time startTime;
    private Time endTime;
    private String status; // 'APPROVED', 'ACTIVE', 'COMPLETED', 'CANCELLED'
    private Timestamp allocatedAt;

    // Joined fields for display
    private String resourceName;
    private String userName;
    private String resourceCategory;

    public Allocation() {}

    public Allocation(int allocationId, int resourceId, int userId, String purpose, 
                      Date bookingDate, Time startTime, Time endTime, String status) {
        this.allocationId = allocationId;
        this.resourceId = resourceId;
        this.userId = userId;
        this.purpose = purpose;
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public int getAllocationId() { return allocationId; }
    public void setAllocationId(int allocationId) { this.allocationId = allocationId; }

    public int getResourceId() { return resourceId; }
    public void setResourceId(int resourceId) { this.resourceId = resourceId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public Date getBookingDate() { return bookingDate; }
    public void setBookingDate(Date bookingDate) { this.bookingDate = bookingDate; }

    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }

    public Time getEndTime() { return endTime; }
    public void setEndTime(Time endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getAllocatedAt() { return allocatedAt; }
    public void setAllocatedAt(Timestamp allocatedAt) { this.allocatedAt = allocatedAt; }

    public String getResourceName() { return resourceName; }
    public void setResourceName(String resourceName) { this.resourceName = resourceName; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getResourceCategory() { return resourceCategory; }
    public void setResourceCategory(String resourceCategory) { this.resourceCategory = resourceCategory; }
}
