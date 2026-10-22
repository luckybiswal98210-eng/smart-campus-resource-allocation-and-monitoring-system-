package com.smartcampus.dao;

import com.smartcampus.config.DatabaseConnection;
import com.smartcampus.model.Allocation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Resource Allocations with conflict detection
 */
public class AllocationDAO {

    public List<Allocation> getAllAllocations() {
        List<Allocation> list = new ArrayList<>();
        String sql = "SELECT a.*, r.name AS resource_name, r.category AS resource_category, u.full_name AS user_name " +
                     "FROM allocations a " +
                     "JOIN resources r ON a.resource_id = r.resource_id " +
                     "JOIN users u ON a.user_id = u.user_id " +
                     "ORDER BY a.booking_date DESC, a.start_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToAllocation(rs));
            }
        } catch (SQLException e) {
            System.err.println("AllocationDAO getAllAllocations error: " + e.getMessage());
        }
        return list;
    }

    public List<Allocation> getUserAllocations(int userId) {
        List<Allocation> list = new ArrayList<>();
        String sql = "SELECT a.*, r.name AS resource_name, r.category AS resource_category, u.full_name AS user_name " +
                     "FROM allocations a " +
                     "JOIN resources r ON a.resource_id = r.resource_id " +
                     "JOIN users u ON a.user_id = u.user_id " +
                     "WHERE a.user_id = ? " +
                     "ORDER BY a.booking_date DESC, a.start_time DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAllocation(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("AllocationDAO getUserAllocations error: " + e.getMessage());
        }
        return list;
    }

    /**
     * Conflict resolution: Check if the resource is already booked for overlapping time slot on given date
     */
    public boolean hasTimeConflict(int resourceId, Date bookingDate, Time startTime, Time endTime, int excludeAllocationId) {
        String sql = "SELECT COUNT(*) FROM allocations " +
                     "WHERE resource_id = ? " +
                     "AND booking_date = ? " +
                     "AND status IN ('APPROVED', 'ACTIVE') " +
                     "AND allocation_id != ? " +
                     "AND (start_time < ? AND end_time > ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            ps.setDate(2, bookingDate);
            ps.setInt(3, excludeAllocationId);
            ps.setTime(4, endTime);
            ps.setTime(5, startTime);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("AllocationDAO hasTimeConflict error: " + e.getMessage());
        }
        return false;
    }

    public boolean addAllocation(Allocation a) {
        String sql = "INSERT INTO allocations (resource_id, user_id, purpose, booking_date, start_time, end_time, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getResourceId());
            ps.setInt(2, a.getUserId());
            ps.setString(3, a.getPurpose());
            ps.setDate(4, a.getBookingDate());
            ps.setTime(5, a.getStartTime());
            ps.setTime(6, a.getEndTime());
            ps.setString(7, a.getStatus());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        a.setAllocationId(gk.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("AllocationDAO addAllocation error: " + e.getMessage());
        }
        return false;
    }

    public boolean updateStatus(int allocationId, String status) {
        String sql = "UPDATE allocations SET status = ? WHERE allocation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, allocationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("AllocationDAO updateStatus error: " + e.getMessage());
            return false;
        }
    }

    private Allocation mapResultSetToAllocation(ResultSet rs) throws SQLException {
        Allocation a = new Allocation();
        a.setAllocationId(rs.getInt("allocation_id"));
        a.setResourceId(rs.getInt("resource_id"));
        a.setUserId(rs.getInt("user_id"));
        a.setPurpose(rs.getString("purpose"));

        // Robust date parsing for MySQL & SQLite
        try {
            a.setBookingDate(rs.getDate("booking_date"));
        } catch (Exception e) {
            String str = rs.getString("booking_date");
            if (str != null) {
                try {
                    a.setBookingDate(Date.valueOf(str.trim().split(" ")[0]));
                } catch (Exception ignored) {}
            }
        }

        // Robust time parsing
        try {
            a.setStartTime(rs.getTime("start_time"));
        } catch (Exception e) {
            String str = rs.getString("start_time");
            if (str != null) {
                str = str.trim();
                if (str.length() == 5) str += ":00";
                try {
                    a.setStartTime(Time.valueOf(str));
                } catch (Exception ignored) {}
            }
        }

        try {
            a.setEndTime(rs.getTime("end_time"));
        } catch (Exception e) {
            String str = rs.getString("end_time");
            if (str != null) {
                str = str.trim();
                if (str.length() == 5) str += ":00";
                try {
                    a.setEndTime(Time.valueOf(str));
                } catch (Exception ignored) {}
            }
        }

        a.setStatus(rs.getString("status"));

        try {
            a.setAllocatedAt(rs.getTimestamp("allocated_at"));
        } catch (Exception ignored) {}
        
        try {
            a.setResourceName(rs.getString("resource_name"));
            a.setResourceCategory(rs.getString("resource_category"));
            a.setUserName(rs.getString("user_name"));
        } catch (SQLException ignored) {}

        return a;
    }
}
