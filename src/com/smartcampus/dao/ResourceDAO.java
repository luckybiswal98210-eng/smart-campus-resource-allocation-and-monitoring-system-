package com.smartcampus.dao;

import com.smartcampus.config.DatabaseConnection;
import com.smartcampus.model.Resource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Campus Resources
 */
public class ResourceDAO {

    public List<Resource> getAllResources() {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT * FROM resources ORDER BY resource_id ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToResource(rs));
            }
        } catch (SQLException e) {
            System.err.println("ResourceDAO getAllResources error: " + e.getMessage());
        }
        return list;
    }

    public Resource getResourceById(int id) {
        String sql = "SELECT * FROM resources WHERE resource_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToResource(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("ResourceDAO getResourceById error: " + e.getMessage());
        }
        return null;
    }

    public List<Resource> searchResources(String keyword, String categoryFilter, String statusFilter) {
        List<Resource> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM resources WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(building) LIKE ? OR LOWER(floor_room) LIKE ?) ");
            String pat = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pat);
            params.add(pat);
            params.add(pat);
        }

        if (categoryFilter != null && !categoryFilter.equals("ALL")) {
            sql.append(" AND category = ? ");
            params.add(categoryFilter);
        }

        if (statusFilter != null && !statusFilter.equals("ALL")) {
            sql.append(" AND status = ? ");
            params.add(statusFilter);
        }

        sql.append(" ORDER BY resource_id ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToResource(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("ResourceDAO searchResources error: " + e.getMessage());
        }
        return list;
    }

    public boolean addResource(Resource res) {
        String sql = "INSERT INTO resources (name, category, building, floor_room, capacity, power_rating_kw, status, features) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, res.getName());
            ps.setString(2, res.getCategory());
            ps.setString(3, res.getBuilding());
            ps.setString(4, res.getFloorRoom());
            ps.setInt(5, res.getCapacity());
            ps.setDouble(6, res.getPowerRatingKw());
            ps.setString(7, res.getStatus());
            ps.setString(8, res.getFeatures());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet gk = ps.getGeneratedKeys()) {
                    if (gk.next()) {
                        res.setResourceId(gk.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("ResourceDAO addResource error: " + e.getMessage());
        }
        return false;
    }

    public boolean updateResource(Resource res) {
        String sql = "UPDATE resources SET name = ?, category = ?, building = ?, floor_room = ?, " +
                     "capacity = ?, power_rating_kw = ?, status = ?, features = ? WHERE resource_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, res.getName());
            ps.setString(2, res.getCategory());
            ps.setString(3, res.getBuilding());
            ps.setString(4, res.getFloorRoom());
            ps.setInt(5, res.getCapacity());
            ps.setDouble(6, res.getPowerRatingKw());
            ps.setString(7, res.getStatus());
            ps.setString(8, res.getFeatures());
            ps.setInt(9, res.getResourceId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ResourceDAO updateResource error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateStatus(int resourceId, String newStatus) {
        String sql = "UPDATE resources SET status = ? WHERE resource_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, resourceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ResourceDAO updateStatus error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteResource(int resourceId) {
        String sql = "DELETE FROM resources WHERE resource_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("ResourceDAO deleteResource error: " + e.getMessage());
            return false;
        }
    }

    private Resource mapResultSetToResource(ResultSet rs) throws SQLException {
        Resource r = new Resource();
        r.setResourceId(rs.getInt("resource_id"));
        r.setName(rs.getString("name"));
        r.setCategory(rs.getString("category"));
        r.setBuilding(rs.getString("building"));
        r.setFloorRoom(rs.getString("floor_room"));
        r.setCapacity(rs.getInt("capacity"));
        r.setPowerRatingKw(rs.getDouble("power_rating_kw"));
        r.setStatus(rs.getString("status"));
        r.setFeatures(rs.getString("features"));
        return r;
    }
}
