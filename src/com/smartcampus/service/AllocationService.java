package com.smartcampus.service;

import com.smartcampus.dao.AllocationDAO;
import com.smartcampus.dao.ResourceDAO;
import com.smartcampus.model.Allocation;
import com.smartcampus.model.Resource;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Business logic service for Resource Allocations and Conflict Resolution
 */
public class AllocationService {

    private final AllocationDAO allocationDAO = new AllocationDAO();
    private final ResourceDAO resourceDAO = new ResourceDAO();

    public static class AllocationResult {
        private final boolean success;
        private final String message;

        public AllocationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    /**
     * Request booking for a campus resource with complete conflict and availability checks
     */
    public AllocationResult requestAllocation(int resourceId, int userId, String purpose, 
                                             Date bookingDate, Time startTime, Time endTime) {
        // 1. Basic validation
        if (purpose == null || purpose.trim().isEmpty()) {
            return new AllocationResult(false, "Please provide the purpose for this allocation request.");
        }

        if (startTime.compareTo(endTime) >= 0) {
            return new AllocationResult(false, "Invalid Time Slot: Start time must be before End time.");
        }

        // Check if resource exists and is not under maintenance
        Resource resource = resourceDAO.getResourceById(resourceId);
        if (resource == null) {
            return new AllocationResult(false, "Selected resource does not exist.");
        }
        if ("MAINTENANCE".equalsIgnoreCase(resource.getStatus())) {
            return new AllocationResult(false, "Cannot book '" + resource.getName() + "'. It is currently undergoing maintenance.");
        }

        // 2. Conflict Detection
        boolean conflict = allocationDAO.hasTimeConflict(resourceId, bookingDate, startTime, endTime, 0);
        if (conflict) {
            return new AllocationResult(false, "Time Conflict Detected! '" + resource.getName() + 
                    "' is already booked for an overlapping slot on " + bookingDate + ".");
        }

        // 3. Create allocation
        Allocation allocation = new Allocation();
        allocation.setResourceId(resourceId);
        allocation.setUserId(userId);
        allocation.setPurpose(purpose.trim());
        allocation.setBookingDate(bookingDate);
        allocation.setStartTime(startTime);
        allocation.setEndTime(endTime);
        allocation.setStatus("APPROVED");

        boolean created = allocationDAO.addAllocation(allocation);
        if (created) {
            // Check if allocation is active right now
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();
            if (bookingDate.toLocalDate().isEqual(today)) {
                LocalTime startLocal = startTime.toLocalTime();
                LocalTime endLocal = endTime.toLocalTime();
                if (!now.isBefore(startLocal) && !now.isAfter(endLocal)) {
                    allocationDAO.updateStatus(allocation.getAllocationId(), "ACTIVE");
                    resourceDAO.updateStatus(resourceId, "ALLOCATED");
                }
            }
            return new AllocationResult(true, "Allocation successfully approved for " + resource.getName() + "!");
        } else {
            return new AllocationResult(false, "Failed to save allocation in database. Please check database logs.");
        }
    }

    /**
     * Release resource early or complete allocation
     */
    public boolean releaseResource(int allocationId, int resourceId) {
        boolean statusUpdated = allocationDAO.updateStatus(allocationId, "COMPLETED");
        if (statusUpdated) {
            resourceDAO.updateStatus(resourceId, "AVAILABLE");
            return true;
        }
        return false;
    }

    /**
     * Cancel an approved allocation
     */
    public boolean cancelAllocation(int allocationId, int resourceId) {
        boolean statusUpdated = allocationDAO.updateStatus(allocationId, "CANCELLED");
        if (statusUpdated) {
            resourceDAO.updateStatus(resourceId, "AVAILABLE");
            return true;
        }
        return false;
    }
}
