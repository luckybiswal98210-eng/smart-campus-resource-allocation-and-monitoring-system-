package com.smartcampus.dao;

import com.smartcampus.config.DatabaseConnection;
import com.smartcampus.model.Telemetry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for IoT Sensor Telemetry & Power Monitoring
 */
public class TelemetryDAO {

    public boolean logTelemetry(Telemetry t) {
        String sql = "INSERT INTO telemetry_logs (resource_id, occupancy_count, power_draw_kw, temperature_c, hvac_status, light_status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getResourceId());
            ps.setInt(2, t.getOccupancyCount());
            ps.setDouble(3, t.getPowerDrawKw());
            ps.setDouble(4, t.getTemperatureC());
            ps.setString(5, t.getHvacStatus());
            ps.setString(6, t.getLightStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("TelemetryDAO logTelemetry error: " + e.getMessage());
            return false;
        }
    }

    public List<Telemetry> getAllLatestTelemetry() {
        List<Telemetry> list = new ArrayList<>();
        // Query to get the latest log for each resource with resource name joined
        String sql = "SELECT t.*, r.name AS resource_name " +
                     "FROM telemetry_logs t " +
                     "JOIN resources r ON t.resource_id = r.resource_id " +
                     "INNER JOIN (" +
                     "   SELECT resource_id, MAX(log_id) as max_id FROM telemetry_logs GROUP BY resource_id" +
                     ") latest ON t.resource_id = latest.resource_id AND t.log_id = latest.max_id " +
                     "ORDER BY t.resource_id ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Telemetry t = new Telemetry();
                t.setLogId(rs.getInt("log_id"));
                t.setResourceId(rs.getInt("resource_id"));
                t.setTimestamp(rs.getTimestamp("timestamp"));
                t.setOccupancyCount(rs.getInt("occupancy_count"));
                t.setPowerDrawKw(rs.getDouble("power_draw_kw"));
                t.setTemperatureC(rs.getDouble("temperature_c"));
                t.setHvacStatus(rs.getString("hvac_status"));
                t.setLightStatus(rs.getString("light_status"));
                t.setResourceName(rs.getString("resource_name"));
                list.add(t);
            }
        } catch (SQLException e) {
            System.err.println("TelemetryDAO getAllLatestTelemetry error: " + e.getMessage());
        }
        return list;
    }

    public double getTotalCurrentPowerDraw() {
        String sql = "SELECT SUM(t.power_draw_kw) " +
                     "FROM telemetry_logs t " +
                     "INNER JOIN (SELECT resource_id, MAX(log_id) as max_id FROM telemetry_logs GROUP BY resource_id) latest " +
                     "ON t.resource_id = latest.resource_id AND t.log_id = latest.max_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            System.err.println("TelemetryDAO getTotalCurrentPowerDraw error: " + e.getMessage());
        }
        return 0.0;
    }

    public int getTotalCurrentOccupancy() {
        String sql = "SELECT SUM(t.occupancy_count) " +
                     "FROM telemetry_logs t " +
                     "INNER JOIN (SELECT resource_id, MAX(log_id) as max_id FROM telemetry_logs GROUP BY resource_id) latest " +
                     "ON t.resource_id = latest.resource_id AND t.log_id = latest.max_id";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("TelemetryDAO getTotalCurrentOccupancy error: " + e.getMessage());
        }
        return 0;
    }
}
