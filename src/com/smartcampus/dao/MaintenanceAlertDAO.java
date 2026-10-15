package com.smartcampus.dao;

import com.smartcampus.config.DatabaseConnection;
import com.smartcampus.model.MaintenanceAlert;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Maintenance Tickets & Hardware Alerts
 */
public class MaintenanceAlertDAO {

    public List<MaintenanceAlert> getAllAlerts() {
        List<MaintenanceAlert> list = new ArrayList<>();
        String sql = "SELECT m.*, r.name AS resource_name " +
                     "FROM maintenance_alerts m " +
                     "JOIN resources r ON m.resource_id = r.resource_id " +
                     "ORDER BY m.is_resolved ASC, m.reported_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                MaintenanceAlert a = new MaintenanceAlert();
                a.setAlertId(rs.getInt("alert_id"));
                a.setResourceId(rs.getInt("resource_id"));
                a.setIssue(rs.getString("issue"));
                a.setSeverity(rs.getString("severity"));
                a.setReportedDate(rs.getDate("reported_date"));
                a.setResolved(rs.getBoolean("is_resolved"));
                a.setResourceName(rs.getString("resource_name"));
                list.add(a);
            }
        } catch (SQLException e) {
            System.err.println("MaintenanceAlertDAO getAllAlerts error: " + e.getMessage());
        }
        return list;
    }

    public boolean addAlert(MaintenanceAlert alert) {
        String sql = "INSERT INTO maintenance_alerts (resource_id, issue, severity, reported_date, is_resolved) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, alert.getResourceId());
            ps.setString(2, alert.getIssue());
            ps.setString(3, alert.getSeverity());
            ps.setDate(4, alert.getReportedDate());
            ps.setBoolean(5, alert.isResolved());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("MaintenanceAlertDAO addAlert error: " + e.getMessage());
            return false;
        }
    }

    public boolean resolveAlert(int alertId) {
        String sql = "UPDATE maintenance_alerts SET is_resolved = TRUE WHERE alert_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, alertId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("MaintenanceAlertDAO resolveAlert error: " + e.getMessage());
            return false;
        }
    }

    public int getActiveAlertCount() {
        String sql = "SELECT COUNT(*) FROM maintenance_alerts WHERE is_resolved = FALSE OR is_resolved = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("MaintenanceAlertDAO getActiveAlertCount error: " + e.getMessage());
        }
        return 0;
    }
}
